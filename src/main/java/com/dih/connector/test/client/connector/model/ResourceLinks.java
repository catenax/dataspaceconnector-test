package com.dih.connector.test.client.connector.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResourceLinks {
    private ResourceLink self;

    public UUID getUUIDFromLink(String apiName) {
        if (self == null || StringUtils.isEmpty(self.getHref())) {
            throw new RuntimeException("Can't get self href");
        }
        var splittedLink = self.getHref().split(apiName);
        if (ArrayUtils.getLength(splittedLink) != 2) {
            throw new RuntimeException("Unexpected size of splitted href. Expected 2, got " + ArrayUtils.getLength(splittedLink));
        }
        return UUID.fromString(splittedLink[1]);
    }
}

