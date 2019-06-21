/*
 * Copyright (c) 2019. Globo.com - ATeam
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Authors: See AUTHORS file
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.globo.pepe.munin.data.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import org.springframework.util.Assert;

@Entity
public class Query extends AbstractEntity {

    @Column(nullable = false)
    private final String value;

    @ManyToOne
    @JoinColumn(name = "provider_id", nullable = false, foreignKey = @ForeignKey(name="FK_query_provider"))
    private final Provider provider;

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false, foreignKey = @ForeignKey(name="FK_query_project"))
    private final Project project;

    public Query(String value, Provider provider, Project project) {
        Assert.hasText(value, "Value must not be null or empty!");
        Assert.notNull(provider, "Provider must not be null!");
        Assert.notNull(project, "Project must not be null!");

        this.value = value;
        this.provider = provider;
        this.project = project;
    }

    public String getValue() {
        return value;
    }

    public Provider getProvider() {
        return provider;
    }

    public Project getProject() {
        return project;
    }
}
