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
package org.os890.bv.addon;

import org.apache.deltaspike.core.api.provider.BeanProvider;

import javax.enterprise.inject.Vetoed;
import javax.validation.MessageInterpolator;
import javax.validation.metadata.ConstraintDescriptor;
import java.io.Serializable;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Vetoed
public class AdvancedMessageInterpolator implements MessageInterpolator, Serializable {
    private static final String DEFAULT_PLACEHOLDER = "{default}";
    private static final String EXPRESSION_START = "{";
    private static final String EXPRESSION_END = "}";

    @Override
    public String interpolate(String messageTemplate, Context context) {
        return interpolate(messageTemplate, context, Locale.getDefault());
    }

    @Override
    public String interpolate(String messageTemplate, Context context, Locale locale) {
        MessageInterpolator defaultMessageInterpolator = BeanProvider.getContextualReference(MessageInterpolatorFactory.class).getDefaultMessageInterpolator();
        List<MessageResolver> messageResolvers = BeanProvider.getContextualReferences(MessageResolver.class, true);

        String result = interpolateText(messageTemplate, context, locale, defaultMessageInterpolator, messageResolvers);

        if (result.contains(EXPRESSION_START) && result.contains(EXPRESSION_END)) {
            return interpolateAdditionalSyntax(result, context, locale, defaultMessageInterpolator, messageResolvers);
        }
        return result;
    }

    private String interpolateAdditionalSyntax(String textToInterpolate, Context context, Locale locale, MessageInterpolator messageInterpolator, List<MessageResolver> messageResolvers) {
        String result = textToInterpolate;
        ConstraintDescriptor<?> constraintDescriptor = context.getConstraintDescriptor();

        Map<String, Object> annotationAttributes = constraintDescriptor.getAttributes();

        for (Map.Entry<String, Object> entry : annotationAttributes.entrySet()) {
            Object attributeValue = entry.getValue();
            if (!(attributeValue instanceof String)) {
                continue;
            }
            String attributeValueAsString = (String) attributeValue;

            if (isSubexpressionToProcess(textToInterpolate, entry.getKey(), attributeValueAsString)) {
                String subMessageKey = (String) entry.getValue();
                if (subMessageKey.equals(DEFAULT_PLACEHOLDER)) {
                    continue;
                }

                String interpolatedSubtext = interpolateText(subMessageKey, context, locale, messageInterpolator, messageResolvers);

                if (!subMessageKey.equals(interpolatedSubtext)) {
                    result = result.replace(attributeValueAsString, interpolatedSubtext);
                }
            }
        }
        return result;
    }

    private String interpolateText(String messageTemplate, Context context, Locale locale, MessageInterpolator defaultMessageInterpolator, List<MessageResolver> messageResolvers) {
        String result = defaultMessageInterpolator.interpolate(messageTemplate, context, locale);

        if (messageTemplate.equals(result) && messageTemplate.startsWith(EXPRESSION_START) && messageTemplate.endsWith(EXPRESSION_END)) {
            for (MessageResolver messageResolver : messageResolvers) {
                result = messageResolver.resolveMessage(messageTemplate.substring(1, messageTemplate.length() - 1), locale);

                if (result.contains(EXPRESSION_START) && result.contains(EXPRESSION_END)) {
                    result = defaultMessageInterpolator.interpolate(result, context, locale); //do the default interpolation/replacement
                }

                if (!messageTemplate.equals(result)) {
                    break;
                }
            }
        }
        return result;
    }

    private boolean isSubexpressionToProcess(String textToInterpolate, String key, String value) {
        return !key.equals("message") && value.startsWith(EXPRESSION_START) && value.endsWith(EXPRESSION_END) &&
            textToInterpolate.contains(value);
    }
}