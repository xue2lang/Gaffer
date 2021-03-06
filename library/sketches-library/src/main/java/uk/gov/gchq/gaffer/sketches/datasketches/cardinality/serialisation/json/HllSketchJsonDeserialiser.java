/*
 * Copyright 2016-2019 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.gchq.gaffer.sketches.datasketches.cardinality.serialisation.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.yahoo.sketches.hll.HllSketch;

import java.io.IOException;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static uk.gov.gchq.gaffer.sketches.datasketches.cardinality.serialisation.json.HllSketchJsonConstants.DEFAULT_LOG_K;
import static uk.gov.gchq.gaffer.sketches.datasketches.cardinality.serialisation.json.HllSketchJsonConstants.VALUES;

/**
 * A {@code HyperLogLogPlusJsonSerialiser} deserialises {@link HllSketch} objects.
 */
public class HllSketchJsonDeserialiser extends JsonDeserializer<HllSketch> {

    @Override
    public HllSketch deserialize(final JsonParser jsonParser, final DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        final TreeNode treeNode = jsonParser.getCodec().readTree(jsonParser);

        final HllSketch hllp;

        final TextNode jsonNodes = (TextNode) treeNode.get(HllSketchJsonConstants.BYTES);
        if (isNull(jsonNodes)) {
            final IntNode kNode = (IntNode) treeNode.get(HllSketchJsonConstants.LOG_K);
            final int k = nonNull(kNode) ? kNode.asInt(DEFAULT_LOG_K) : DEFAULT_LOG_K;
            hllp = new HllSketch(k);
        } else {
            hllp = HllSketch.heapify(jsonNodes.binaryValue());
        }

        final ArrayNode offers = (ArrayNode) treeNode.get(VALUES);
        if (nonNull(offers)) {
            for (final JsonNode offer : offers) {
                if (nonNull(offer)) {
                    hllp.update(offer.asText());
                }
            }
        }

        return hllp;
    }
}
