/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.os890.cdi;

import org.apache.deltaspike.core.api.exclude.Exclude;
import org.apache.deltaspike.core.api.projectstage.ProjectStage.Development;
import org.os890.bv.demo.TestPerson;
import org.os890.ejb.ViolationMessageService;
import org.os890.jpa.ViolationMessage;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.validation.Validator;
import java.util.Locale;
import java.util.logging.Logger;

@Exclude(exceptIfProjectStage = Development.class)
public class DevInitializer {
    private static final Logger LOG = Logger.getLogger(DevInitializer.class.getName());

    @Inject
    private ViolationMessageService violationMessageService;

    @Inject
    private Validator validator;

    public void onStartup(@Observes @Initialized(ApplicationScoped.class) Object startupEvent) {
        violationMessageService.save(new ViolationMessage("firstName", "Vorname", Locale.getDefault()));
        violationMessageService.save(new ViolationMessage("lastName", "Nachname", Locale.getDefault()));
        violationMessageService.save(new ViolationMessage("nameLength", "'{propertyName}' muss min. {min} Zeichen enthalten", Locale.getDefault()));

        LOG.info(">>>" + validator.validate(new TestPerson("g", "p")).iterator().next().getMessage());
    }
}
