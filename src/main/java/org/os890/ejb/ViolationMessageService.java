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
package org.os890.ejb;

import org.os890.jpa.ViolationMessage;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.Locale;

@Stateless
public class ViolationMessageService {
    @PersistenceContext(unitName = "demoPU")
    private EntityManager entityManager;

    public void save(ViolationMessage violationMessage) {
        entityManager.persist(violationMessage);
    }

    public String findMessageTextFor(String key, Locale locale) {
        TypedQuery<ViolationMessage> query = entityManager.createQuery("select vm from ViolationMessage vm where vm.key = :msgKey and vm.locale = :locale", ViolationMessage.class);
        if (key.startsWith("{") && key.endsWith("}")) {
            query.setParameter("msgKey", key.substring(1, key.length() - 1));
        } else {
            query.setParameter("msgKey", key);
        }
        query.setParameter("locale", locale.toString());

        try {
            return query.getSingleResult().getMessage();
        } catch (NoResultException e) {
            return key;
        }
    }
}
