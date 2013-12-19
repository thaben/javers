package org.javers.core.diff.appenders

import org.javers.core.diff.AbstractDiffTest
import org.javers.core.diff.NodePair
import org.javers.core.model.DummyUser
import org.javers.core.model.DummyUserDetails
import org.javers.model.domain.Change
import org.javers.model.domain.GlobalCdoId
import org.javers.model.mapping.Property
import org.javers.model.object.graph.ObjectNode

import static org.javers.test.ReferenceChangesAssert.assertThat

import static org.javers.test.builder.DummyUserBuilder.dummyUser
import static org.javers.test.builder.DummyUserDetailsBuilder.dummyUserDetails


class ReferenceChangeAppenderTest extends AbstractDiffTest{

    def "should not append change when the same references"() {
        given:
        DummyUser leftReference = dummyUser().withName("1").withDetails(2).build()
        DummyUser rightReference = leftReference;
        ObjectNode leftNode = buildGraph(leftReference)
        ObjectNode rightNode = buildGraph(rightReference)
        Property property = getEntity(DummyUser).getProperty("dummyUserDetails")

        when:
        Collection<Change> changes =
            new ReferenceChangeAppender().calculateChanges(new NodePair(leftNode, rightNode), property)

        then:
        assertThat changes hasSize 0
    }

    def "should compare refs null safely"() {
        given:
        ObjectNode leftNode =  buildGraph(dummyUser().withName("1").withDetails(2).build())
        ObjectNode rightNode = buildGraph(dummyUser().withName("1").build())
        Property property = getEntity(DummyUser).getProperty("dummyUserDetails")

        when:
        Collection<Change> changes =
            new ReferenceChangeAppender().calculateChanges(new NodePair(leftNode, rightNode), property)

        then:
        assertThat(changes)
                .hasSize(1)
                .assertThatFirstChange()
                .hasCdoId("1")
                .hasLeftReference(DummyUserDetails,2)
                .hasRightReference(null)
                .hasProperty(property)
    }

    def "should append reference change"() {
        given:
        ObjectNode leftNode =  buildGraph(dummyUser().withName("1").withDetails(2).build())
        ObjectNode rightNode = buildGraph(dummyUser().withName("1").withDetails(3).build())
        Property property = getEntity(DummyUser).getProperty("dummyUserDetails")

        when:
        Collection<Change> changes =
            new ReferenceChangeAppender().calculateChanges(new NodePair(leftNode, rightNode), property)

        then:
        assertThat(changes)
            .hasSize(1)
            .assertThatFirstChange()
            .hasCdoId("1")
            .hasLeftReference(DummyUserDetails,2)
            .hasRightReference(DummyUserDetails,3)
            .hasProperty(property)
    }

}
