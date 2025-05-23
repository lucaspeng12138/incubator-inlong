/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.inlong.manager.service.source.mqtt;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.inlong.manager.common.consts.SourceType;
import org.apache.inlong.manager.common.enums.ErrorCodeEnum;
import org.apache.inlong.manager.common.exceptions.BusinessException;
import org.apache.inlong.manager.common.util.CommonBeanUtils;
import org.apache.inlong.manager.dao.entity.StreamSourceEntity;
import org.apache.inlong.manager.pojo.source.SourceRequest;
import org.apache.inlong.manager.pojo.source.StreamSource;
import org.apache.inlong.manager.pojo.source.mqtt.MqttSource;
import org.apache.inlong.manager.pojo.source.mqtt.MqttSourceDTO;
import org.apache.inlong.manager.pojo.source.mqtt.MqttSourceRequest;
import org.apache.inlong.manager.pojo.stream.StreamField;
import org.apache.inlong.manager.service.source.AbstractSourceOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MqttSourceOperator extends AbstractSourceOperator {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public Boolean accept(String sourceType) {
        return SourceType.MQTT.equals(sourceType);
    }

    @Override
    protected String getSourceType() {
        return SourceType.MQTT;
    }

    @Override
    protected void setTargetEntity(SourceRequest request, StreamSourceEntity targetEntity) {
        MqttSourceRequest sourceRequest = (MqttSourceRequest) request;
        CommonBeanUtils.copyProperties(sourceRequest, targetEntity, true);
        try {
            MqttSourceDTO dto = MqttSourceDTO.getFromRequest(sourceRequest);
            targetEntity.setExtParams(objectMapper.writeValueAsString(dto));
        } catch (Exception e) {
            throw new BusinessException(ErrorCodeEnum.SOURCE_INFO_INCORRECT,
                    String.format("serialize extParams of Mqtt SourceDTO failure: %s", e.getMessage()));
        }
    }

    @Override
    public StreamSource getFromEntity(StreamSourceEntity entity) {
        MqttSource source = new MqttSource();
        if (entity == null) {
            return source;
        }

        MqttSourceDTO dto = MqttSourceDTO.getFromJson(entity.getExtParams());
        CommonBeanUtils.copyProperties(entity, source, true);
        CommonBeanUtils.copyProperties(dto, source, true);

        List<StreamField> streamFields = super.getSourceFields(entity.getId());
        source.setFieldList(streamFields);
        return source;
    }
}
