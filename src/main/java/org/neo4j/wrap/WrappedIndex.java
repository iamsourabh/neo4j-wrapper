/**
 * Copyright (c) 2002-2011 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.wrap;

import java.util.Iterator;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.graphdb.index.ReadableIndex;
import org.neo4j.graphdb.index.ReadableRelationshipIndex;
import org.neo4j.graphdb.index.RelationshipIndex;

import static org.neo4j.wrap.WrappedEntity.unwrap;

public abstract class WrappedIndex<T extends PropertyContainer, I extends ReadableIndex<T>> extends WrappedObject<I>
        implements Index<T>
{
    private WrappedIndex( WrappedGraphDatabase graphdb, I index )
    {
        super( graphdb, index );
    }

    @SuppressWarnings( "unchecked" )
    static <T extends PropertyContainer> Index<T> unwrapIndex( Index<T> index )
    {
        if ( index instanceof WrappedIndex<?, ?> )
        {
            return ( (WrappedIndex<T, Index<T>>) index ).wrapped;
        }
        else
        {
            return index;
        }
    }

    abstract T wrap( T entity );

    @Override
    public String getName()
    {
        return wrapped.getName();
    }

    @Override
    public Class<T> getEntityType()
    {
        return wrapped.getEntityType();
    }

    @Override
    public void add( T entity, String key, Object value )
    {
        if ( wrapped instanceof Index<?> )
        {
            ( (Index<T>) wrapped ).add( unwrap( entity ), key, value );
        }
        else
        {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public void remove( T entity, String key, Object value )
    {
        if ( wrapped instanceof Index<?> )
        {
            ( (Index<T>) wrapped ).remove( unwrap( entity ), key, value );
        }
        else
        {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public void remove( T entity, String key )
    {
        if ( wrapped instanceof Index<?> )
        {
            ( (Index<T>) wrapped ).remove( unwrap( entity ), key );
        }
        else
        {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public void remove( T entity )
    {
        if ( wrapped instanceof Index<?> )
        {
            ( (Index<T>) wrapped ).remove( unwrap( entity ) );
        }
        else
        {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public void delete()
    {
        if ( wrapped instanceof Index<?> )
        {
            ( (Index<T>) wrapped ).delete();
        }
        else
        {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public IndexHits<T> get( String key, Object value )
    {
        return new Hits( wrapped.get( key, value ) );
    }

    @Override
    public IndexHits<T> query( String key, Object queryOrQueryObject )
    {
        return new Hits( wrapped.query( key, queryOrQueryObject ) );
    }

    @Override
    public IndexHits<T> query( Object queryOrQueryObject )
    {
        return new Hits( wrapped.query( queryOrQueryObject ) );
    }

    public static class WrappedNodeIndex<G extends WrappedGraphDatabase> extends
            WrappedIndex<Node, ReadableIndex<Node>>
    {
        protected WrappedNodeIndex( G graphdb, ReadableIndex<Node> index )
        {
            super( graphdb, index );
        }

        @SuppressWarnings( "unchecked" )
        protected G graphdb()
        {
            return (G) graphdb;
        }

        @Override
        Node wrap( Node entity )
        {
            return graphdb.node( entity, false );
        }
    }

    public static class WrappedRelationshipIndex<G extends WrappedGraphDatabase> extends
            WrappedIndex<Relationship, ReadableRelationshipIndex> implements RelationshipIndex
    {
        protected WrappedRelationshipIndex( G graphdb, ReadableRelationshipIndex index )
        {
            super( graphdb, index );
        }

        @SuppressWarnings( "unchecked" )
        protected G graphdb()
        {
            return (G) graphdb;
        }

        @Override
        public IndexHits<Relationship> get( String key, Object valueOrNull, Node startNodeOrNull, Node endNodeOrNull )
        {
            return new Hits( wrapped.get( key, valueOrNull, unwrap( startNodeOrNull ), unwrap( endNodeOrNull ) ) );
        }

        @Override
        public IndexHits<Relationship> query( String key, Object queryOrQueryObjectOrNull, Node startNodeOrNull,
                Node endNodeOrNull )
        {
            return new Hits( wrapped.query( key, queryOrQueryObjectOrNull, unwrap( startNodeOrNull ),
                    unwrap( endNodeOrNull ) ) );
        }

        @Override
        public IndexHits<Relationship> query( Object queryOrQueryObjectOrNull, Node startNodeOrNull, Node endNodeOrNull )
        {
            return new Hits(
                    wrapped.query( queryOrQueryObjectOrNull, unwrap( startNodeOrNull ), unwrap( endNodeOrNull ) ) );
        }

        @Override
        Relationship wrap( Relationship entity )
        {
            return graphdb.relationship( entity, false );
        }
    }

    private static abstract class WrappedIndexHits<T> implements IndexHits<T>
    {
        private final IndexHits<T> hits;

        WrappedIndexHits( IndexHits<T> hits )
        {
            this.hits = hits;
        }

        abstract T wrap( T item );

        @Override
        public int hashCode()
        {
            return hits.hashCode();
        }

        @Override
        public boolean equals( Object obj )
        {
            if ( this == obj ) return true;
            if ( obj instanceof WrappedIndex.WrappedIndexHits )
            {
                @SuppressWarnings( "rawtypes" ) WrappedIndex.WrappedIndexHits other = (WrappedIndex.WrappedIndexHits) obj;
                return hits.equals( other.hits );
            }
            return false;
        }

        @Override
        public String toString()
        {
            return hits.toString();
        }

        @Override
        public boolean hasNext()
        {
            return hits.hasNext();
        }

        @Override
        public T next()
        {
            return wrap( hits.next() );
        }

        @Override
        public void remove()
        {
            hits.remove();
        }

        @Override
        public Iterator<T> iterator()
        {
            return this;
        }

        @Override
        public int size()
        {
            return hits.size();
        }

        @Override
        public void close()
        {
            hits.close();
        }

        @Override
        public T getSingle()
        {
            T single = hits.getSingle();
            if (single == null) return null;
            return wrap( single );
        }

        @Override
        public float currentScore()
        {
            return hits.currentScore();
        }
    }

    class Hits extends WrappedIndexHits<T>
    {
        Hits( IndexHits<T> hits )
        {
            super( hits );
        }

        @Override
        T wrap( T item )
        {
            return WrappedIndex.this.wrap( item );
        }
    }
}
