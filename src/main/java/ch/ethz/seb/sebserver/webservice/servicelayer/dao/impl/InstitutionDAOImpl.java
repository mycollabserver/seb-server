/*
 * Copyright (c) 2018 ETH Zürich, Educational Development and Technology (LET)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package ch.ethz.seb.sebserver.webservice.servicelayer.dao.impl;

import static org.mybatis.dynamic.sql.SqlBuilder.*;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.dynamic.sql.SqlBuilder;
import org.mybatis.dynamic.sql.select.MyBatis3SelectModelAdapter;
import org.mybatis.dynamic.sql.select.QueryExpressionDSL;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ch.ethz.seb.sebserver.gbl.model.EntityKey;
import ch.ethz.seb.sebserver.gbl.model.EntityType;
import ch.ethz.seb.sebserver.gbl.model.institution.Institution;
import ch.ethz.seb.sebserver.gbl.util.Result;
import ch.ethz.seb.sebserver.webservice.datalayer.batis.mapper.InstitutionRecordDynamicSqlSupport;
import ch.ethz.seb.sebserver.webservice.datalayer.batis.mapper.InstitutionRecordMapper;
import ch.ethz.seb.sebserver.webservice.datalayer.batis.model.InstitutionRecord;
import ch.ethz.seb.sebserver.webservice.servicelayer.bulkaction.BulkAction;
import ch.ethz.seb.sebserver.webservice.servicelayer.dao.DAOLoggingSupport;
import ch.ethz.seb.sebserver.webservice.servicelayer.dao.FilterMap;
import ch.ethz.seb.sebserver.webservice.servicelayer.dao.InstitutionDAO;
import ch.ethz.seb.sebserver.webservice.servicelayer.dao.ResourceNotFoundException;
import ch.ethz.seb.sebserver.webservice.servicelayer.dao.TransactionHandler;

@Lazy
@Component
public class InstitutionDAOImpl implements InstitutionDAO {

    private final InstitutionRecordMapper institutionRecordMapper;

    public InstitutionDAOImpl(final InstitutionRecordMapper institutionRecordMapper) {
        this.institutionRecordMapper = institutionRecordMapper;
    }

    @Override
    public EntityType entityType() {
        return EntityType.INSTITUTION;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean exists(final String name) {
        if (StringUtils.isBlank(name)) {
            return false;
        }

        final Long count = this.institutionRecordMapper.countByExample()
                .where(InstitutionRecordDynamicSqlSupport.name, isEqualTo(name))
                .build()
                .execute();

        return count != null && count.longValue() > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public Result<Institution> byPK(final Long id) {
        return recordById(id)
                .flatMap(InstitutionDAOImpl::toDomainModel);
    }

    @Override
    @Transactional(readOnly = true)
    public Result<Collection<Institution>> all(final Long institutionId, final Boolean active) {
        return Result.tryCatch(() -> {
            final QueryExpressionDSL<MyBatis3SelectModelAdapter<List<InstitutionRecord>>> example =
                    this.institutionRecordMapper.selectByExample();

            final List<InstitutionRecord> records = (active != null)
                    ? example
                            .where(
                                    InstitutionRecordDynamicSqlSupport.id,
                                    isEqualToWhenPresent(institutionId))
                            .and(
                                    InstitutionRecordDynamicSqlSupport.active,
                                    isEqualToWhenPresent(BooleanUtils.toIntegerObject(active)))
                            .build()
                            .execute()
                    : example.build().execute();

            return records.stream()
                    .map(InstitutionDAOImpl::toDomainModel)
                    .flatMap(DAOLoggingSupport::logUnexpectedErrorAndSkip)
                    .collect(Collectors.toList());
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Result<Collection<Institution>> allMatching(
            final FilterMap filterMap,
            final Predicate<Institution> predicate) {

        return Result.tryCatch(() -> this.institutionRecordMapper
                .selectByExample()
                .where(
                        InstitutionRecordDynamicSqlSupport.active,
                        SqlBuilder.isEqualToWhenPresent(filterMap.getActiveAsInt()))
                .and(
                        InstitutionRecordDynamicSqlSupport.name,
                        SqlBuilder.isEqualToWhenPresent(filterMap.getName()))
                .build()
                .execute()
                .stream()
                .map(InstitutionDAOImpl::toDomainModel)
                .flatMap(DAOLoggingSupport::logUnexpectedErrorAndSkip)
                .filter(predicate)
                .collect(Collectors.toList()));
    }

    @Override
    @Transactional
    public Result<Institution> save(final String modelId, final Institution institution) {
        return Result.tryCatch(() -> {

            final Long pk = Long.parseLong(modelId);
            final InstitutionRecord newRecord = new InstitutionRecord(
                    pk,
                    institution.name,
                    institution.urlSuffix,
                    null,
                    institution.logoImage);

            this.institutionRecordMapper.updateByPrimaryKeySelective(newRecord);
            return this.institutionRecordMapper.selectByPrimaryKey(pk);
        })
                .flatMap(InstitutionDAOImpl::toDomainModel)
                .onErrorDo(TransactionHandler::rollback);
    }

    @Override
    @Transactional
    public Result<Institution> createNew(final Institution institution) {
        return Result.tryCatch(() -> {
            final InstitutionRecord newRecord = new InstitutionRecord(
                    null,
                    institution.name,
                    institution.urlSuffix,
                    BooleanUtils.toInteger(false),
                    institution.logoImage);

            this.institutionRecordMapper.insert(newRecord);
            return newRecord;
        })
                .flatMap(InstitutionDAOImpl::toDomainModel)
                .onErrorDo(TransactionHandler::rollback);
    }

    @Override
    @Transactional
    public Result<Collection<EntityKey>> setActive(final Set<EntityKey> all, final boolean active) {
        return Result.tryCatch(() -> {

            final List<Long> ids = extractPKsFromKeys(all);
            final InstitutionRecord institutionRecord = new InstitutionRecord(
                    null, null, null, BooleanUtils.toInteger(active), null);

            this.institutionRecordMapper.updateByExampleSelective(institutionRecord)
                    .where(InstitutionRecordDynamicSqlSupport.id, isIn(ids))
                    .build()
                    .execute();

            return ids.stream()
                    .map(id -> new EntityKey(id, EntityType.INSTITUTION))
                    .collect(Collectors.toList());
        });
    }

    @Override
    @Transactional
    public Result<Collection<EntityKey>> delete(final Set<EntityKey> all) {
        return Result.tryCatch(() -> {

            final List<Long> ids = extractPKsFromKeys(all);

            this.institutionRecordMapper.deleteByExample()
                    .where(InstitutionRecordDynamicSqlSupport.id, isIn(ids))
                    .build()
                    .execute();

            return ids.stream()
                    .map(id -> new EntityKey(id, EntityType.INSTITUTION))
                    .collect(Collectors.toList());
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Set<EntityKey> getDependencies(final BulkAction bulkAction) {
        // NOTE since Institution is the top most Entity, there are no other Entity for that an Institution depends on.
        return Collections.emptySet();
    }

    @Override
    @Transactional(readOnly = true)
    public Result<Collection<Institution>> loadEntities(final Collection<EntityKey> keys) {
        return Result.tryCatch(() -> {
            final List<Long> ids = extractPKsFromKeys(keys);

            return this.institutionRecordMapper.selectByExample()
                    .where(InstitutionRecordDynamicSqlSupport.id, isIn(ids))
                    .build()
                    .execute()
                    .stream()
                    .map(InstitutionDAOImpl::toDomainModel)
                    .flatMap(DAOLoggingSupport::logUnexpectedErrorAndSkip)
                    .collect(Collectors.toList());
        });
    }

    private Result<InstitutionRecord> recordById(final Long id) {
        return Result.tryCatch(() -> {
            final InstitutionRecord record = this.institutionRecordMapper.selectByPrimaryKey(id);
            if (record == null) {
                throw new ResourceNotFoundException(
                        EntityType.INSTITUTION,
                        String.valueOf(id));
            }
            return record;
        });
    }

    private static Result<Institution> toDomainModel(final InstitutionRecord record) {
        return Result.tryCatch(() -> new Institution(
                record.getId(),
                record.getName(),
                record.getUrlSuffix(),
                record.getLogoImage(),
                BooleanUtils.toBooleanObject(record.getActive())));
    }

}