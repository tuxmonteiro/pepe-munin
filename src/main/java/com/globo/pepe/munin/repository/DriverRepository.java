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

import com.globo.pepe.common.model.munin.Driver;
import com.globo.pepe.common.model.munin.Driver.Type;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource(path = "driver", collectionResourceRel = "driver", itemResourceRel = "driver", exported = false)
public interface DriverRepository extends JpaRepository<Driver, Long> {

    List<Driver> findByTypeAndJarNotNull(Type type);

}
