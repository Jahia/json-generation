/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2019 Jahia Solutions Group SA. All rights reserved.
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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Christophe Laprun
 */
@XmlRootElement
@JsonDeserialize(using = JSONProperties.PropertiesDeserializer.class)
public class JSONProperties<D extends JSONDecorator<D>> extends JSONSubElementContainer<D> {
    @XmlElement
    private Map<String, JSONProperty<D>> properties;

    private JSONProperties() {
        super(null);
    }

    protected JSONProperties(JSONNode<D> parent, Node node) throws RepositoryException {
        this(parent, node, Filter.OUTPUT_ALL);
    }

    protected JSONProperties(JSONNode<D> parent, Node node, Filter filter) throws RepositoryException {
        super(parent);
        initWith(parent, node, filter);
    }

    @Override
    public String getSubElementContainerName() {
        return JSONConstants.PROPERTIES;
    }

    public void initWith(JSONNode<D> parent, Node node, Filter filter) throws RepositoryException {
        super.initWith(parent, JSONConstants.PROPERTIES);

        final String[] nameGlobs = filter.acceptedPropertyNameGlobs();
        final PropertyIterator props = nameGlobs == null ? node.getProperties() : node.getProperties(nameGlobs);

        // properties URI builder
        if (props != null) {
            properties = new HashMap<String, JSONProperty<D>>((int) props.getSize());
            while (props.hasNext()) {
                Property property = props.nextProperty();

                if (filter.acceptProperty(property)) {
                    // add property
                    this.properties.put(Names.escape(property.getName()), new JSONProperty<D>(getNewDecoratorOrNull(), property));
                }
            }
        }
    }

    public Map<String, JSONProperty<D>> getProperties() {
        return properties;
    }

    public void addProperty(String name, JSONProperty<D> property) {
        if (properties == null) {
            properties = new HashMap<String, JSONProperty<D>>(7);
        }
        properties.put(name, property);
    }

    public static class PropertiesDeserializer extends JsonDeserializer<JSONProperties> {
        @Override
        public JSONProperties deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            ObjectCodec codec = parser.getCodec();
            ObjectNode root = codec.readTree(parser);

            final int size = root.size();
            if (size > 0) {
                final JSONProperties properties = new JSONProperties();
                final Iterator<Map.Entry<String, JsonNode>> fields = root.fields();
                while (fields.hasNext()) {
                    final Map.Entry<String, JsonNode> field = fields.next();
                    properties.addProperty(field.getKey(), codec.treeToValue(field.getValue(), JSONProperty.class));
                }

                return properties;
            } else {
                return null;
            }
        }
    }
}
