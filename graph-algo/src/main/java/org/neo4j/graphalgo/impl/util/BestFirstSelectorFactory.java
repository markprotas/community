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
package org.neo4j.graphalgo.impl.util;

import java.util.HashSet;
import java.util.Set;

import org.neo4j.graphalgo.impl.util.PriorityMap.Converter;
import org.neo4j.graphalgo.impl.util.PriorityMap.Entry;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.traversal.BranchOrderingPolicy;
import org.neo4j.graphdb.traversal.BranchSelector;
import org.neo4j.graphdb.traversal.TraversalBranch;

public abstract class BestFirstSelectorFactory<P extends Comparable<P>, D>
        implements BranchOrderingPolicy
{
    public BranchSelector create( TraversalBranch startSource )
    {
        return new BestFirstSelector( startSource, getStartData() );
    }
    
    protected abstract P getStartData();

    public final class BestFirstSelector implements BranchSelector
    {
        private PriorityMap<TraversalBranch, Node, P> queue =
                PriorityMap.withNaturalOrder( CONVERTER );
        private TraversalBranch current;
        private P currentAggregatedValue;
        private final Set<Long> visitedNodes = new HashSet<Long>();

        public BestFirstSelector( TraversalBranch source, P startData )
        {
            this.current = source;
            this.currentAggregatedValue = startData;
        }

        public TraversalBranch next()
        {
            // Exhaust current if not already exhausted
            while ( true )
            {
                TraversalBranch next = current.next();
                if ( next != null )
                {
                    if ( !visitedNodes.contains( next.node().getId() ) )
                    {
                        P newPriority = addPriority( next, currentAggregatedValue,
                                calculateValue( next ) );
                        queue.put( next, newPriority );
                    }
                }
                else
                {
                    break;
                }
            }
            
            // Pop the top from priorityMap
            Entry<TraversalBranch, P> entry = queue.pop();
            if ( entry != null )
            {
                current = entry.getEntity();
                currentAggregatedValue = entry.getPriority();
                visitedNodes.add( current.node().getId() );
                return current;
            }
            return null;
        }
    }

    protected abstract P addPriority( TraversalBranch source,
            P currentAggregatedValue, D value );
    
    protected abstract D calculateValue( TraversalBranch next );
    
    public static final Converter<Node, TraversalBranch> CONVERTER =
            new Converter<Node, TraversalBranch>()
    {
        public Node convert( TraversalBranch source )
        {
            return source.node();
        }
    };
}
