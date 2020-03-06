package org.javers.spring.auditable.integration

import org.javers.core.Javers
import org.javers.repository.jql.QueryBuilder
import org.javers.spring.auditable.aspect.JaversAuditableAspectAsync
import org.javers.spring.model.DummyObject
import org.javers.spring.repository.DummyAuditedAsyncRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

@ContextConfiguration(classes = [TestApplicationConfig])
class JaversAuditableAspectAsyncIntegrationTest extends Specification {

    @Autowired
    Javers javers

    @Autowired
    JaversAuditableAspectAsync javersAuditableAspectAsync

    @Autowired
    DummyAuditedAsyncRepository repository

    @Autowired
    JaversAuditableAspectAsync aspectAsync

    def "should asynchronously commit a method's argument when annotated with @JaversAuditableAsync"() {
        given:
        def o = new DummyObject()

        assert javersAuditableAspectAsync.lastAsyncCommit.isEmpty()

        when:
        repository.save(o)

        then:
        def query = QueryBuilder.byInstanceId(o.id, DummyObject).build()

        !javersAuditableAspectAsync.lastAsyncCommit.get().isDone()

        when:
        waitForCommit(o)

        then:
        javersAuditableAspectAsync.lastAsyncCommit.get().isDone()
        def snapshot = javers.findSnapshots(query)[0]

        snapshot.globalId.cdoId == o.id
        snapshot.commitMetadata.properties["key"] == "ok"

    }

    def "should asynchronously commit two method's arguments when annotated with @JaversAuditableAsync"() {
        given:
        def o1 = new DummyObject()
        def o2 = new DummyObject()

        when:
        repository.saveTwo(o1, o2)

        then:
        !javersAuditableAspectAsync.lastAsyncCommit.get().isDone()

        when:
        waitForCommit(o1, o2)

        then:
        javersAuditableAspectAsync.lastAsyncCommit.get().isDone()
        javers.findSnapshots(QueryBuilder.byInstanceId(o1.id, DummyObject).build()).size() == 1
        javers.findSnapshots(QueryBuilder.byInstanceId(o2.id, DummyObject).build()).size() == 1
    }

    def "should asynchronously commit an iterable argument when method is annotated with @JaversAuditableAsync"() {
        given:
        def o1 = new DummyObject()
        def o2 = new DummyObject()
        def objects = [o1, o2]

        when:
        repository.saveAll(objects)

        then:
        !javersAuditableAspectAsync.lastAsyncCommit.get().isDone()

        when:
        waitForCommit(o1, o2)

        then:
        javersAuditableAspectAsync.lastAsyncCommit.get().isDone()
        javers.findSnapshots(QueryBuilder.byInstanceId(o1.id, DummyObject).build()).size() == 1
        javers.findSnapshots(QueryBuilder.byInstanceId(o2.id, DummyObject).build()).size() == 1
    }

    void waitForCommit(DummyObject... objects) {
        println "waitForCommit..."
        long start = new Date().time
        for (int i=0; i<50; i++) {

            def sizes = objects.collect{o ->
                def query = QueryBuilder.byInstanceId(o.id, DummyObject).build()
                javers.findSnapshots(query).size()
            }
            println("sizes : " + sizes)

            if (sizes.sum() >= objects.size()) {
                long stop = new Date().time
                println "awaited " + (stop - start) + " millis"
                break
            }

            println("$i - wait 50ms ...")
            sleep(50)
        }
    }
}
