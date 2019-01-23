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
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;
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
@JsonDeserialize(using = JSONMixins.MixinsDeserializer.class)
public class JSONMixins<D extends JSONDecorator<D>> extends JSONSubElementContainer<D> {
    @XmlElement
    private Map<String, JSONMixin<D>> mixins;

    private JSONMixins() {
        super(null);
    }

    protected JSONMixins(JSONNode<D> parent, Node node) throws RepositoryException {
        this(parent, node, Filter.OUTPUT_ALL);
    }

    protected JSONMixins(JSONNode<D> parent, Node node, Filter filter) throws RepositoryException {
        super(parent);

        final NodeType[] mixinNodeTypes = node.getMixinNodeTypes();
        if (mixinNodeTypes != null) {
            mixins = new HashMap<String, JSONMixin<D>>(mixinNodeTypes.length);
            for (NodeType mixinNodeType : mixinNodeTypes) {
                if (filter.acceptMixin(mixinNodeType)) {
                    final String name = mixinNodeType.getName();
                    mixins.put(Names.escape(name), new JSONMixin<D>(getNewDecoratorOrNull(), node, mixinNodeType));
                }
            }
        }
    }

    @Override
    public String getSubElementContainerName() {
        return JSONConstants.MIXINS;
    }

    public Map<String, JSONMixin<D>> getMixins() {
        return mixins;
    }

    public static class MixinsDeserializer extends JsonDeserializer<JSONMixins> {
        @Override
        public JSONMixins deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            ObjectCodec codec = parser.getCodec();
            ObjectNode root = codec.readTree(parser);

            final int size = root.size();
            if (size > 0) {
                final JSONMixins mixins = new JSONMixins();
                final Iterator<Map.Entry<String, JsonNode>> fields = root.fields();
                while (fields.hasNext()) {
                    final Map.Entry<String, JsonNode> field = fields.next();
                    mixins.addChild(field.getKey(), codec.treeToValue(field.getValue(), JSONMixin.class));
                }

                return mixins;
            } else {
                return null;
            }
        }
    }

    private void addChild(String name, JSONMixin<D> mixin) {
        if (mixins == null) {
            mixins = new HashMap<String, JSONMixin<D>>(7);
        }

        mixins.put(name, mixin);
    }
}
