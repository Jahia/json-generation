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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Christophe Laprun
 */
@XmlRootElement
@JsonDeserialize(using = JSONChildren.ChildrenDeserializer.class)
public class JSONChildren<D extends JSONDecorator<D>> extends JSONSubElementContainer<D> {
    @XmlElement
    private Map<String, JSONNode<D>> children;

    private JSONChildren() {
        super(null);
    }

    protected JSONChildren(JSONNode<D> parent, Node node) throws RepositoryException {
        this(parent, node, Filter.OUTPUT_ALL, 0);
    }

    protected JSONChildren(JSONNode<D> parent, Node node, Filter filter, int depth) throws RepositoryException {
        super(parent);
        initWith(parent, node, filter, depth);
    }

    @Override
    public String getSubElementContainerName() {
        return JSONConstants.CHILDREN;
    }

    protected void initWith(JSONNode<D> parent, Node node, Filter filter, int depth) throws RepositoryException {
        super.initWith(parent, JSONConstants.CHILDREN);

        String[] nameGlobs = filter.acceptedChildNameGlobs();
        final NodeIterator nodes = nameGlobs == null ? node.getNodes() : node.getNodes(nameGlobs);
        children = initChildrenMap();

        while (nodes.hasNext()) {
            Node child = nodes.nextNode();

            if (filter.acceptChild(child)) {
                children.put(Names.escape(child.getName(), child.getIndex()), new JSONNode<D>(getNewDecoratorOrNull(), child, filter, depth - 1));
            }
        }
    }

    private Map<String, JSONNode<D>> initChildrenMap() {
        return new LinkedHashMap<String, JSONNode<D>>();
    }

    public Map<String, JSONNode<D>> getChildren() {
        return Collections.unmodifiableMap(children);
    }

    public static class ChildrenDeserializer extends JsonDeserializer<JSONChildren> {
        @Override
        public JSONChildren deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            ObjectCodec codec = parser.getCodec();
            ObjectNode root = codec.readTree(parser);

            final int size = root.size();
            if (size > 0) {
                final JSONChildren children = new JSONChildren();
                final Iterator<Map.Entry<String, JsonNode>> fields = root.fields();
                while (fields.hasNext()) {
                    final Map.Entry<String, JsonNode> field = fields.next();
                    children.addChild(field.getKey(), codec.treeToValue(field.getValue(), JSONNode.class));
                }

                return children;
            } else {
                return null;
            }
        }
    }

    private void addChild(String name, JSONNode<D> child) {
        if (children == null) {
            children = initChildrenMap();
        }

        children.put(name, child);
    }
}
