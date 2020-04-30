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

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Map;

/**
 * A JSON representation of a JCR node. <p/>
 * <pre>
 * "name" : <the node's unescaped name>,
 * "type" : <the node's node type name>,
 * "properties" : <properties representation>,
 * "mixins" : <mixins representation>,
 * "children" : <children representation>,
 * "versions" : <versions representation>,
 * "links" : {
 * "self" : "<URI identifying the resource associated with this node>",
 * "type" : "<URI identifying the resource associated with this node's type>",
 * "properties" : "<URI identifying the resource associated with this node's properties>",
 * "mixins" : "<URI identifying the resource associated with this node's mixins>",
 * "children" : "<URI identifying the resource associated with this node's children>",
 * "versions" : "<URI identifying the resource associated with this node's versions>"
 * }
 * </pre>
 *
 * @author Christophe Laprun
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class JSONNode<D extends JSONDecorator<D>> extends JSONItem<Node, D> {
    private JSONMixins<D> mixins;
    private JSONVersions<D> versions;
    protected JSONProperties<D> properties;
    protected JSONChildren<D> children;

    @XmlElement
    protected String id;

    private JSONNode() {
        this(null);
    }

    protected JSONNode(D decorator) {
        super(decorator);
    }

    protected JSONNode(D decorator, Node node, Filter filter, int depth) throws RepositoryException {
        this(decorator);
        initWith(node, filter == null ? Filter.OUTPUT_ALL : filter, depth);
    }

    protected void initWith(Node node, Filter filter, int depth) throws RepositoryException {
        super.initWith(node);
        id = node.getIdentifier();

        if (depth > 0) {
            properties = filter.outputProperties() ? new JSONProperties<D>(this, node, filter) : null;

            mixins = filter.outputMixins() ? new JSONMixins<D>(this, node, filter) : null;

            children = filter.outputChildren() ? new JSONChildren<D>(this, node, filter, depth) : null;

            versions = filter.outputVersions() ? new JSONVersions<D>(this, node, filter) : null;

            getDecoratorOrNullOpIfNull().initFrom(this);
        } else {
            properties = null;
            mixins = null;
            children = null;
            versions = null;
        }
    }

    @Override
    public String getUnescapedTypeName(Node item) throws RepositoryException {
        return item.getPrimaryNodeType().getName();
    }

    public JSONChildren<D> getJSONChildren() {
        return children;
    }

    @XmlElement
    public Map<String, JSONNode<D>> getChildren() {
        return children != null ? children.getChildren() : null;
    }

    public JSONProperties<D> getJSONProperties() {
        return properties;
    }

    @XmlElement
    public Map<String, JSONProperty<D>> getProperties() {
        return properties != null ? properties.getProperties() : null;
    }

    public JSONProperty<D> getProperty(String property) {
        final Map<String, JSONProperty<D>> properties = getProperties();
        if (properties != null && !properties.isEmpty()) {
            property = Names.escape(property);
            return properties.get(property);
        } else {
            return null;
        }
    }

    public JSONMixins<D> getJSONMixins() {
        return mixins;
    }

    @XmlElement
    public Map<String, JSONMixin<D>> getMixins() {
        return mixins != null ? mixins.getMixins() : null;
    }

    public JSONVersions<D> getJSONVersions() {
        return versions;
    }

    @XmlElement
    public Map<String, JSONVersion<D>> getVersions() {
        return versions != null ? versions.getVersions() : null;
    }

    public String getId() {
        return id;
    }
}
