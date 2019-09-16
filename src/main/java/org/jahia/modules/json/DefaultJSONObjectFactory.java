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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
import org.jahia.modules.json.jcr.SessionAccess;

import javax.jcr.*;

/**
 * @author Christophe Laprun
 */
@SuppressWarnings("unused")
public class DefaultJSONObjectFactory extends JSONObjectFactory {
    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        final TypeFactory typeFactory = mapper.getTypeFactory();
        mapper.setAnnotationIntrospector(AnnotationIntrospector.pair(new JacksonAnnotationIntrospector(), new JaxbAnnotationIntrospector(typeFactory)));
    }

    private DefaultJSONObjectFactory() {
    }

    // Initialization on demand holder idiom: thread-safe singleton initialization
    private static class Holder {
        static final DefaultJSONObjectFactory INSTANCE = new DefaultJSONObjectFactory();

        private Holder() {
        }
    }

    public static DefaultJSONObjectFactory getInstance() {
        return Holder.INSTANCE;
    }

    @Override
    public JSONDecorator createDecorator() {
        return null;
    }

    public String getAsString(Item item) {
        return getAsString(item, null, Filter.OUTPUT_ALL);
    }

    public String getAsString(Item item, Session session, Filter filter) {
        if (filter == null) {
            filter = Filter.OUTPUT_ALL;
        }

        // record current session info if any
        final SessionAccess.SessionInfo currentSession = SessionAccess.getCurrentSession();

        if (session != null) {
            // change the current session to use the provided one
            SessionAccess.setCurrentSession(session, session.getWorkspace().getName(), null);
        }

        try {
            JSONBase base;
            if (item instanceof Node) {
                base = createNode((Node) item, filter, 1);
            }
            else {
                base = createProperty((Property) item);
            }

            return getAsString(base);
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        } finally {
            if (currentSession != null) {
                // restore the previous session information
                SessionAccess.setCurrentSession(currentSession.session, currentSession.workspace, currentSession.language);
            }
        }
    }

    public static String getAsString(JSONBase jsonBase) {
        try {
            return mapper.writeValueAsString(jsonBase);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
