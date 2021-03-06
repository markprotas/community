/**
 * Copyright (c) 2002-2012 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.server.rest.repr;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import com.tinkerpop.blueprints.pgm.impls.neo4j.Neo4jEdge;
import com.tinkerpop.blueprints.pgm.impls.neo4j.Neo4jGraph;
import com.tinkerpop.blueprints.pgm.impls.neo4j.Neo4jVertex;
import com.tinkerpop.pipes.util.structures.Table;

public class ObjectToRepresentationConverter
{
    public static Representation convert( final Object data )
    {
        if ( data instanceof Table )
        {
            return new GremlinTableRepresentation( (Table) data );
        }
        if ( data instanceof Iterable )
        {
            return getListRepresentation( (Iterable) data );
        }
        if ( data instanceof Iterator )
        {
            Iterator iterator = (Iterator) data;
            return getIteratorRepresentation( iterator );
        }
        if ( data instanceof Map )
        {
            
            return getMapRepresentation( (Map) data );
        }
        return getSingleRepresentation( data );
    }

    public static MappingRepresentation getMapRepresentation( Map data )
    {
        
        return new MapRepresentation( data );
    }

    static Representation getIteratorRepresentation( Iterator data )
    {
        final List<Representation> results = new ArrayList<Representation>();
        while ( data.hasNext() )
        {
            Object value = data.next();
            if ( value instanceof Iterable )
            {
                List<Representation> nested = new ArrayList<Representation>();
                nested.addAll( convertValuesToRepresentations( (Iterable) value ) );
                results.add( new ListRepresentation( getType( nested ), nested ) );
            }
            final Representation representation = getSingleRepresentation( value );
            results.add( representation );
        }
        return new ListRepresentation( getType( results ), results );
    }

    public static ListRepresentation getListRepresentation( Iterable data )
    {
        final List<Representation> results = convertValuesToRepresentations( data );
        return new ListRepresentation( getType( results ), results );
    }

    static List<Representation> convertValuesToRepresentations( Iterable data )
    {
        final List<Representation> results = new ArrayList<Representation>();
        if ( data instanceof Table )
        {
            results.add( new GremlinTableRepresentation( (Table) data ) );
        }
        else
        {
            for ( final Object value : data )
            {
                if ( value instanceof Iterable )
                {
                    List<Representation> nested = new ArrayList<Representation>();
                    nested.addAll( convertValuesToRepresentations( (Iterable) value ) );
                    results.add( new ListRepresentation( getType( nested ),
                            nested ) );
                }
                else
                {
                    results.add( getSingleRepresentation( value ) );
                }
            }
        }
        return results;
    }

    static RepresentationType getType( List<Representation> representations )
    {
        if ( representations == null || representations.isEmpty() )
            return RepresentationType.STRING;
        return representations.get( 0 ).getRepresentationType();
    }

    static Representation getSingleRepresentation( Object result )
    {
        if ( result == null ) return ValueRepresentation.string( "null" );
        if ( result instanceof Neo4jVertex )
        {
            return new NodeRepresentation(
                    ( (Neo4jVertex) result ).getRawVertex() );
        }
        else if ( result instanceof Neo4jEdge )
        {
            return new RelationshipRepresentation(
                    ( (Neo4jEdge) result ).getRawEdge() );
        }
        else if ( result instanceof GraphDatabaseService )
        {
            return new DatabaseRepresentation( ( (GraphDatabaseService) result ) );
        }
        else if ( result instanceof Node )
        {
            return new NodeRepresentation( (Node) result );
        }
        else if ( result instanceof Relationship )
        {
            return new RelationshipRepresentation( (Relationship) result );
        }
        else if ( result instanceof Neo4jGraph )
        {
            return ValueRepresentation.string( ( (Neo4jGraph) result ).getRawGraph().toString() );
        }
        else if ( result instanceof Double || result instanceof Float )
        {
            return ValueRepresentation.number( ( (Number) result ).doubleValue() );
        }
        else if ( result instanceof Long )
        {
            return ValueRepresentation.number( ( (Long) result ).longValue() );
        }
        else if ( result instanceof Integer )
        {
            return ValueRepresentation.number( ( (Integer) result ).intValue() );
        }
        else
        {
            return ValueRepresentation.string( result.toString() );
        }
    }
}
