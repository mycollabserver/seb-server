/*
 * Copyright (c) 2019 ETH Zürich, Educational Development and Technology (LET)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package ch.ethz.seb.sebserver.webservice.servicelayer.dao.impl;

import static org.mybatis.dynamic.sql.SqlBuilder.isEqualTo;
import static org.mybatis.dynamic.sql.SqlBuilder.isIn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.mybatis.dynamic.sql.SqlBuilder;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ch.ethz.seb.sebserver.gbl.api.EntityType;
import ch.ethz.seb.sebserver.gbl.model.EntityKey;
import ch.ethz.seb.sebserver.gbl.model.sebconfig.Orientation;
import ch.ethz.seb.sebserver.gbl.profile.WebServiceProfile;
import ch.ethz.seb.sebserver.gbl.util.Result;
import ch.ethz.seb.sebserver.webservice.datalayer.batis.mapper.OrientationRecordDynamicSqlSupport;
import ch.ethz.seb.sebserver.webservice.datalayer.batis.mapper.OrientationRecordMapper;
import ch.ethz.seb.sebserver.webservice.datalayer.batis.model.OrientationRecord;
import ch.ethz.seb.sebserver.webservice.servicelayer.dao.DAOLoggingSupport;
import ch.ethz.seb.sebserver.webservice.servicelayer.dao.FilterMap;
import ch.ethz.seb.sebserver.webservice.servicelayer.dao.OrientationDAO;
import ch.ethz.seb.sebserver.webservice.servicelayer.dao.ResourceNotFoundException;
import ch.ethz.seb.sebserver.webservice.servicelayer.dao.TransactionHandler;

@Lazy
@Component
@WebServiceProfile
public class OrientationDAOImpl implements OrientationDAO {

    private final OrientationRecordMapper orientationRecordMapper;

    protected OrientationDAOImpl(final OrientationRecordMapper orientationRecordMapper) {
        this.orientationRecordMapper = orientationRecordMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public EntityType entityType() {
        return EntityType.ORIENTATION;
    }

    @Override
    @Transactional(readOnly = true)
    public Result<Orientation> byPK(final Long id) {
        return recordById(id)
                .flatMap(OrientationDAOImpl::toDomainModel);
    }

    @Override
    @Transactional(readOnly = true)
    public Result<Collection<Orientation>> allOf(final Set<Long> pks) {
        return Result.tryCatch(() -> {
            return this.orientationRecordMapper.selectByExample()
                    .where(OrientationRecordDynamicSqlSupport.id, isIn(new ArrayList<>(pks)))
                    .build()
                    .execute()
                    .stream()
                    .map(OrientationDAOImpl::toDomainModel)
                    .flatMap(DAOLoggingSupport::logUnexpectedErrorAndSkip)
                    .collect(Collectors.toList());
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Result<Collection<Orientation>> allMatching(
            final FilterMap filterMap,
            final Predicate<Orientation> predicate) {

        return Result.tryCatch(() -> this.orientationRecordMapper
                .selectByExample()
                .where(
                        OrientationRecordDynamicSqlSupport.template,
                        SqlBuilder.isEqualToWhenPresent(filterMap.getOrientationTemplate()))
                .and(
                        OrientationRecordDynamicSqlSupport.view,
                        SqlBuilder.isEqualToWhenPresent(filterMap.getOrientationView()))
                .and(
                        OrientationRecordDynamicSqlSupport.group,
                        SqlBuilder.isEqualToWhenPresent(filterMap.getOrientationGroup()))
                .build()
                .execute()
                .stream()
                .map(OrientationDAOImpl::toDomainModel)
                .flatMap(DAOLoggingSupport::logUnexpectedErrorAndSkip)
                .filter(predicate)
                .collect(Collectors.toList()));
    }

    @Override
    @Transactional
    public Result<Orientation> createNew(final Orientation data) {
        return Result.tryCatch(() -> {

            final OrientationRecord newRecord = new OrientationRecord(
                    null,
                    data.attributeId,
                    data.template,
                    data.view,
                    data.group,
                    data.xPosition,
                    data.yPosition,
                    data.width,
                    data.height);

            this.orientationRecordMapper.insert(newRecord);
            return newRecord;
        })
                .flatMap(OrientationDAOImpl::toDomainModel)
                .onErrorDo(TransactionHandler::rollback);
    }

    @Override
    @Transactional
    public Result<Orientation> save(final Orientation data) {
        return Result.tryCatch(() -> {

            final OrientationRecord newRecord = new OrientationRecord(
                    data.id,
                    null,
                    null,
                    null,
                    data.group,
                    data.xPosition,
                    data.yPosition,
                    data.width,
                    data.height);

            this.orientationRecordMapper.updateByPrimaryKeySelective(newRecord);
            return this.orientationRecordMapper.selectByPrimaryKey(data.id);
        })
                .flatMap(OrientationDAOImpl::toDomainModel)
                .onErrorDo(TransactionHandler::rollback);
    }

    @Override
    @Transactional
    public Result<Collection<EntityKey>> delete(final Set<EntityKey> all) {
        return Result.tryCatch(() -> {

            final List<Long> ids = extractListOfPKs(all);

            this.orientationRecordMapper.deleteByExample()
                    .where(OrientationRecordDynamicSqlSupport.id, isIn(ids))
                    .build()
                    .execute();

            return ids.stream()
                    .map(id -> new EntityKey(id, EntityType.ORIENTATION))
                    .collect(Collectors.toList());
        });
    }

    @Override
    @Transactional
    public Result<Collection<EntityKey>> deleteAllOfTemplate(final String templateName) {
        return Result.tryCatch(() -> {

            final List<Long> ids = this.orientationRecordMapper.selectIdsByExample()
                    .where(OrientationRecordDynamicSqlSupport.template, isEqualTo(templateName))
                    .build()
                    .execute();

            this.orientationRecordMapper.deleteByExample()
                    .where(OrientationRecordDynamicSqlSupport.template, isEqualTo(templateName))
                    .build()
                    .execute();

            return ids.stream()
                    .map(id -> new EntityKey(id, EntityType.ORIENTATION))
                    .collect(Collectors.toList());
        });
    }

    private Result<OrientationRecord> recordById(final Long id) {
        return Result.tryCatch(() -> {
            final OrientationRecord record = this.orientationRecordMapper.selectByPrimaryKey(id);
            if (record == null) {
                throw new ResourceNotFoundException(
                        EntityType.ORIENTATION,
                        String.valueOf(id));
            }
            return record;
        });
    }

    private static Result<Orientation> toDomainModel(final OrientationRecord record) {
        return Result.tryCatch(() -> new Orientation(
                record.getId(),
                record.getConfigAttributeId(),
                record.getTemplate(),
                record.getView(),
                record.getGroup(),
                record.getxPosition(),
                record.getyPosition(),
                record.getWidth(),
                record.getHeight()));
    }

}