/*
 * Copyright (c) 2019 ETH Zürich, Educational Development and Technology (LET)
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
import org.mybatis.dynamic.sql.select.MyBatis3SelectModelAdapter;
import org.mybatis.dynamic.sql.select.QueryExpressionDSL;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ch.ethz.seb.sebserver.gbl.model.EntityKey;
import ch.ethz.seb.sebserver.gbl.model.EntityType;
import ch.ethz.seb.sebserver.gbl.model.institution.LmsSetup;
import ch.ethz.seb.sebserver.gbl.model.institution.LmsSetup.LmsType;
import ch.ethz.seb.sebserver.gbl.util.Result;
import ch.ethz.seb.sebserver.webservice.datalayer.batis.mapper.LmsSetupRecordDynamicSqlSupport;
import ch.ethz.seb.sebserver.webservice.datalayer.batis.mapper.LmsSetupRecordMapper;
import ch.ethz.seb.sebserver.webservice.datalayer.batis.model.LmsSetupRecord;
import ch.ethz.seb.sebserver.webservice.servicelayer.bulkaction.BulkAction;
import ch.ethz.seb.sebserver.webservice.servicelayer.dao.DAOLoggingSupport;
import ch.ethz.seb.sebserver.webservice.servicelayer.dao.FilterMap;
import ch.ethz.seb.sebserver.webservice.servicelayer.dao.LmsSetupDAO;
import ch.ethz.seb.sebserver.webservice.servicelayer.dao.ResourceNotFoundException;
import ch.ethz.seb.sebserver.webservice.servicelayer.dao.TransactionHandler;

@Lazy
@Component
public class LmsSetupDAOImpl implements LmsSetupDAO {

    private final LmsSetupRecordMapper lmsSetupRecordMapper;

    public LmsSetupDAOImpl(final LmsSetupRecordMapper lmsSetupRecordMapper) {
        this.lmsSetupRecordMapper = lmsSetupRecordMapper;
    }

    @Override
    public EntityType entityType() {
        return EntityType.LMS_SETUP;
    }

    @Override
    @Transactional(readOnly = true)
    public Result<LmsSetup> byPK(final Long id) {
        return recordById(id)
                .flatMap(LmsSetupDAOImpl::toDomainModel);
    }

    @Override
    @Transactional(readOnly = true)
    public Result<Collection<LmsSetup>> all(final Long institutionId, final Boolean active) {
        return Result.tryCatch(() -> {
            final QueryExpressionDSL<MyBatis3SelectModelAdapter<List<LmsSetupRecord>>> example =
                    this.lmsSetupRecordMapper.selectByExample();

            final List<LmsSetupRecord> records = (active != null)
                    ? example
                            .where(
                                    LmsSetupRecordDynamicSqlSupport.institutionId,
                                    isEqualToWhenPresent(institutionId))
                            .and(
                                    LmsSetupRecordDynamicSqlSupport.active,
                                    isEqualToWhenPresent(BooleanUtils.toIntegerObject(active)))
                            .build()
                            .execute()
                    : example.build().execute();

            return records.stream()
                    .map(LmsSetupDAOImpl::toDomainModel)
                    .flatMap(DAOLoggingSupport::logUnexpectedErrorAndSkip)
                    .collect(Collectors.toList());
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Result<Collection<LmsSetup>> allMatching(
            final FilterMap filterMap,
            final Predicate<LmsSetup> predicate) {

        return Result.tryCatch(() -> {

            return this.lmsSetupRecordMapper
                    .selectByExample()
                    .where(
                            LmsSetupRecordDynamicSqlSupport.institutionId,
                            isEqualToWhenPresent(filterMap.getInstitutionId()))
                    .and(
                            LmsSetupRecordDynamicSqlSupport.name,
                            isLikeWhenPresent(filterMap.getLmsSetupName()))
                    .and(
                            LmsSetupRecordDynamicSqlSupport.lmsType,
                            isEqualToWhenPresent(filterMap.getLmsSetupType()))
                    .and(
                            LmsSetupRecordDynamicSqlSupport.active,
                            isEqualToWhenPresent(filterMap.getActiveAsInt()))
                    .build()
                    .execute()
                    .stream()
                    .map(LmsSetupDAOImpl::toDomainModel)
                    .flatMap(DAOLoggingSupport::logUnexpectedErrorAndSkip)
                    .filter(predicate)
                    .collect(Collectors.toList());
        });
    }

    @Override
    @Transactional
    public Result<LmsSetup> save(final String modelId, final LmsSetup lmsSetup) {
        return Result.tryCatch(() -> {

            final Long pk = Long.parseLong(modelId);
            final LmsSetupRecord newRecord = new LmsSetupRecord(
                    pk,
                    lmsSetup.institutionId,
                    lmsSetup.name,
                    (lmsSetup.lmsType != null) ? lmsSetup.lmsType.name() : null,
                    lmsSetup.lmsApiUrl,
                    lmsSetup.lmsAuthName,
                    lmsSetup.lmsAuthSecret,
                    lmsSetup.lmsRestApiToken,
                    lmsSetup.sebAuthName,
                    lmsSetup.sebAuthSecret,
                    null);

            this.lmsSetupRecordMapper.updateByPrimaryKeySelective(newRecord);
            return this.lmsSetupRecordMapper.selectByPrimaryKey(pk);
        })
                .flatMap(LmsSetupDAOImpl::toDomainModel)
                .onErrorDo(TransactionHandler::rollback);
    }

    @Override
    @Transactional
    public Result<LmsSetup> createNew(final LmsSetup lmsSetup) {
        return Result.tryCatch(() -> {

            final LmsSetupRecord newRecord = new LmsSetupRecord(
                    null,
                    lmsSetup.institutionId,
                    lmsSetup.name,
                    (lmsSetup.lmsType != null) ? lmsSetup.lmsType.name() : null,
                    lmsSetup.lmsApiUrl,
                    lmsSetup.lmsAuthName,
                    lmsSetup.lmsAuthSecret,
                    lmsSetup.lmsRestApiToken,
                    lmsSetup.sebAuthName,
                    lmsSetup.sebAuthSecret,
                    BooleanUtils.toInteger(false));

            this.lmsSetupRecordMapper.insert(newRecord);
            return newRecord;
        })
                .flatMap(LmsSetupDAOImpl::toDomainModel)
                .onErrorDo(TransactionHandler::rollback);
    }

    @Override
    @Transactional
    public Result<Collection<EntityKey>> setActive(final Set<EntityKey> all, final boolean active) {
        return Result.tryCatch(() -> {

            final List<Long> ids = extractPKsFromKeys(all);
            final LmsSetupRecord lmsSetupRecord = new LmsSetupRecord(
                    null, null, null, null, null, null, null, null, null, null,
                    BooleanUtils.toIntegerObject(active));

            this.lmsSetupRecordMapper.updateByExampleSelective(lmsSetupRecord)
                    .where(LmsSetupRecordDynamicSqlSupport.id, isIn(ids))
                    .build()
                    .execute();

            return ids.stream()
                    .map(id -> new EntityKey(id, EntityType.LMS_SETUP))
                    .collect(Collectors.toList());
        });
    }

    @Override
    @Transactional
    public Result<Collection<EntityKey>> delete(final Set<EntityKey> all) {
        return Result.tryCatch(() -> {

            final List<Long> ids = extractPKsFromKeys(all);

            this.lmsSetupRecordMapper.deleteByExample()
                    .where(LmsSetupRecordDynamicSqlSupport.id, isIn(ids))
                    .build()
                    .execute();

            return ids.stream()
                    .map(id -> new EntityKey(id, EntityType.LMS_SETUP))
                    .collect(Collectors.toList());
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Set<EntityKey> getDependencies(final BulkAction bulkAction) {
        // all of institution
        if (bulkAction.sourceType == EntityType.INSTITUTION) {
            return getDependencies(bulkAction, this::allIdsOfInstitution);
        }

        return Collections.emptySet();
    }

    @Override
    @Transactional(readOnly = true)
    public Result<Collection<LmsSetup>> loadEntities(final Collection<EntityKey> keys) {
        return Result.tryCatch(() -> {
            final List<Long> ids = extractPKsFromKeys(keys);

            return this.lmsSetupRecordMapper.selectByExample()
                    .where(LmsSetupRecordDynamicSqlSupport.id, isIn(ids))
                    .build()
                    .execute()
                    .stream()
                    .map(LmsSetupDAOImpl::toDomainModel)
                    .flatMap(DAOLoggingSupport::logUnexpectedErrorAndSkip)
                    .collect(Collectors.toList());
        });
    }

    private Result<Collection<EntityKey>> allIdsOfInstitution(final EntityKey institutionKey) {
        return Result.tryCatch(() -> {
            return this.lmsSetupRecordMapper.selectIdsByExample()
                    .where(LmsSetupRecordDynamicSqlSupport.institutionId,
                            isEqualTo(Long.valueOf(institutionKey.modelId)))
                    .build()
                    .execute()
                    .stream()
                    .map(id -> new EntityKey(id, EntityType.LMS_SETUP))
                    .collect(Collectors.toList());
        });
    }

    private Result<LmsSetupRecord> recordById(final Long id) {
        return Result.tryCatch(() -> {
            final LmsSetupRecord record = this.lmsSetupRecordMapper.selectByPrimaryKey(id);
            if (record == null) {
                throw new ResourceNotFoundException(
                        EntityType.LMS_SETUP,
                        String.valueOf(id));
            }
            return record;
        });
    }

    private static Result<LmsSetup> toDomainModel(final LmsSetupRecord record) {
        return Result.tryCatch(() -> new LmsSetup(
                record.getId(),
                record.getInstitutionId(),
                record.getName(),
                LmsType.valueOf(record.getLmsType()),
                record.getLmsClientname(),
                record.getLmsClientsecret(),
                record.getLmsUrl(),
                record.getLmsRestApiToken(),
                record.getSebClientname(),
                record.getSebClientsecret(),
                BooleanUtils.toBooleanObject(record.getActive())));
    }

}
