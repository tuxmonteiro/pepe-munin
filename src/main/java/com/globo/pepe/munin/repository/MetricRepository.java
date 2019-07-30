/*
 * Copyright (c) 2019. Globo.com - ATeam
 * All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Authors: See AUTHORS file
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.globo.pepe.munin.repository;

import com.globo.pepe.common.model.munin.Metric;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "metric", collectionResourceRel = "metric", itemResourceRel = "metric", exported = false)
public interface MetricRepository extends JpaRepository<Metric, Long> {

    @Query(value =
        "SELECT m.id FROM metric as m "
        + "WHERE m.metric_enable "
        + "AND (UNIX_TIMESTAMP(CURRENT_TIMESTAMP) - "
        + "UNIX_TIMESTAMP(m.last_processing)) > m.interval_time"
     , nativeQuery = true)
    List<Long> selectAllByNeedToProcess();

    List<Metric> findByIdIn(List<Long> ids);

}
