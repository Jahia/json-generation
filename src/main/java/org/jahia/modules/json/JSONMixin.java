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
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.PropertyDefinition;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Christophe Laprun
 */
@XmlRootElement
public class JSONMixin<D extends JSONDecorator<D>> extends JSONNamed<D> {
    @XmlElement
    private Map<String, String> properties;

    @XmlElement
    private String type;

    protected JSONMixin(D decorator) {
        super(decorator);
    }

    protected JSONMixin(D decorator, Node nodeWithMixin, NodeType item) throws RepositoryException {
        this(decorator);
        initWith(nodeWithMixin, item);
    }

    public void initWith(Node parentNode, NodeType item) throws RepositoryException {
        // todo: should we try to point to the actual mixin definition instead of pointing to the relative path to the mixin in the context of the parent node
        // todo: should we add parent link?
        super.initWith(item.getName());

        this.type = item.getName();

        final PropertyDefinition[] propertyDefinitions = item.getDeclaredPropertyDefinitions();
        if (propertyDefinitions != null) {
            properties = new HashMap<String, String>(propertyDefinitions.length);
            for (PropertyDefinition property : propertyDefinitions) {
                properties.put(property.getName(), JSONProperty.getHumanReadablePropertyType(property.getRequiredType()));
            }
        }

        getDecoratorOrNullOpIfNull().initFrom(this);
    }

    public String getType() {
        return type;
    }
}
