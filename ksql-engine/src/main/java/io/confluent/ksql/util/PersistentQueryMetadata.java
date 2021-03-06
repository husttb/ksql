/*
 * Copyright 2018 Confluent Inc.
 *
 * Licensed under the Confluent Community License; you may not use this file
 * except in compliance with the License.  You may obtain a copy of the License at
 *
 * http://www.confluent.io/confluent-community-license
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */

package io.confluent.ksql.util;

import io.confluent.ksql.metastore.KsqlTopic;
import io.confluent.ksql.metastore.StructuredDataSource;
import io.confluent.ksql.planner.plan.OutputNode;
import io.confluent.ksql.query.QueryId;
import io.confluent.ksql.serde.DataSource;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.Topology;

public class PersistentQueryMetadata extends QueryMetadata {

  private final QueryId id;
  private final KsqlTopic resultTopic;

  private final Set<String> sinkNames;

  // CHECKSTYLE_RULES.OFF: ParameterNumberCheck
  public PersistentQueryMetadata(final String statementString,
                                 final KafkaStreams kafkaStreams,
                                 final OutputNode outputNode,
                                 final StructuredDataSource sinkDataSource,
                                 final String executionPlan,
                                 final QueryId id,
                                 final DataSource.DataSourceType dataSourceType,
                                 final String queryApplicationId,
                                 final KafkaTopicClient kafkaTopicClient,
                                 final KsqlTopic resultTopic,
                                 final Topology topology,
                                 final Map<String, Object> overriddenProperties) {
    // CHECKSTYLE_RULES.ON: ParameterNumberCheck
    super(statementString, kafkaStreams, outputNode, executionPlan, dataSourceType,
          queryApplicationId, kafkaTopicClient, topology, overriddenProperties);
    this.id = id;
    this.resultTopic = resultTopic;
    this.sinkNames = new HashSet<>();
    this.sinkNames.add(sinkDataSource.getName());
  }

  public QueryId getQueryId() {
    return id;
  }

  public KsqlTopic getResultTopic() {
    return resultTopic;
  }

  public String getEntity() {
    return getOutputNode().getId().toString();
  }

  public Set<String> getSinkNames() {
    return sinkNames;
  }

  public DataSource.DataSourceSerDe getResultTopicSerde() {
    if (resultTopic.getKsqlTopicSerDe() == null) {
      throw new KsqlException(String.format("Invalid result topic: %s. Serde cannot be null.",
                                            resultTopic.getName()));
    }
    return resultTopic.getKsqlTopicSerDe().getSerDe();
  }

  @Override
  public boolean equals(final Object o) {
    if (!(o instanceof PersistentQueryMetadata)) {
      return false;
    }

    final PersistentQueryMetadata that = (PersistentQueryMetadata) o;

    return Objects.equals(this.id, that.id) && super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, super.hashCode());
  }
}
