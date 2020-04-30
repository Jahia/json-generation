/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2020 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.modules.json;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import javax.jcr.Item;
import javax.jcr.RepositoryException;
import javax.jcr.version.Version;
import javax.xml.bind.annotation.XmlElement;
import java.io.IOException;

/**
 * @author Christophe Laprun
 */
public abstract class JSONBase<T extends JSONDecorator<T>> {
    private T decorator;

    protected JSONBase(T decorator) {
        this.decorator = decorator;
    }

    @JsonUnwrapped
    @JsonDeserialize(using = DecoratorDeserializer.class)
    @XmlElement
    public T getDecorator() {
        return decorator;
    }

    protected JSONDecorator<T> getDecoratorOrNullOpIfNull() {
        return decorator != null ? decorator : new NullDecorator();
    }

    protected T getNewDecoratorOrNull() {
        return decorator == null ? null : decorator.newInstance();
    }

    private class NullDecorator implements JSONDecorator<T> {

        @Override
        public void initFrom(JSONSubElementContainer<T> subElementContainer) {

        }

        @Override
        public <I extends Item> void initFrom(JSONItem<I, T> jsonItem, I item) throws RepositoryException {

        }

        @Override
        public void initFrom(JSONNode<T> jsonNode) {

        }

        @Override
        public void initFrom(JSONProperty<T> jsonProperty) throws RepositoryException {

        }

        @Override
        public T newInstance() {
            return null;
        }

        @Override
        public void initFrom(JSONVersion<T> jsonVersion, Version version) throws RepositoryException {

        }

        @Override
        public void initFrom(JSONMixin<T> jsonMixin) {

        }
    }

    public static class DecoratorDeserializer extends JsonDeserializer<Object> {

        @Override
        public Object deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            // for now assume that decorator data is never passed to the API and therefore doesn't need to be deserialized
            // if we ever need to deserialize, we should probably register decorators with this deserializer and use the tactics as described
            // in http://programmerbruce.blogspot.fr/2011/05/deserialize-json-with-jackson-into.html section #6 where the decorator class
            // would be selected based on the unique name of its root element
            return null;
        }
    }
}
