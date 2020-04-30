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
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.version.Version;

/**
 * @author Christophe Laprun
 */
@SuppressWarnings("unused")
public abstract class JSONObjectFactory<T extends JSONDecorator<T>> {

    public abstract T createDecorator();

    public JSONNode<T> createNode(Node node, int depth) throws RepositoryException {
        return createNode(node, Filter.OUTPUT_ALL, depth);
    }

    public JSONNode<T> createNode(Node node, Filter filter, int depth) throws RepositoryException {
        return new JSONNode<T>(createDecorator(), node, filter, depth);
    }

    public JSONChildren<T> createChildren(JSONNode<T> parent, Node node) throws RepositoryException {
        return createChildren(parent, node, Filter.OUTPUT_ALL, 0);
    }

    public JSONChildren<T> createChildren(JSONNode<T> parent, Node node, Filter filter, int depth) throws RepositoryException {
        return new JSONChildren<T>(parent, node, filter, depth);
    }

    public JSONVersions<T> createVersions(JSONNode<T> parent, Node node) throws RepositoryException {
        return createVersions(parent, node, Filter.OUTPUT_ALL);
    }

    public JSONVersions<T> createVersions(JSONNode<T> parent, Node node, Filter filter) throws RepositoryException {
        return new JSONVersions<T>(parent, node, filter);
    }

    public JSONVersion<T> createVersion(Node node, Version version) throws RepositoryException {
        return new JSONVersion<T>(createDecorator(), node, version);
    }

    public JSONProperties<T> createProperties(JSONNode<T> parent, Node node) throws RepositoryException {
        return createProperties(parent, node, Filter.OUTPUT_ALL);
    }

    public JSONProperties<T> createProperties(JSONNode<T> parent, Node node, Filter filter) throws RepositoryException {
        return new JSONProperties<T>(parent, node, filter);
    }

    public JSONMixin<T> createMixin(Node node, NodeType mixin) throws RepositoryException {
        return new JSONMixin<T>(createDecorator(), node, mixin);
    }

    public JSONMixins<T> createMixins(JSONNode<T> parent, Node node) throws RepositoryException {
        return createMixins(parent, node, Filter.OUTPUT_ALL);
    }

    public JSONMixins<T> createMixins(JSONNode<T> parent, Node node, Filter filter) throws RepositoryException {
        return new JSONMixins<T>(parent, node, filter);
    }

    public JSONProperty<T> createProperty(Property property) throws RepositoryException {
        return new JSONProperty<T>(createDecorator(), property);
    }
}
