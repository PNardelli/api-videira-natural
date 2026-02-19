package com.api.uteis;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class Util {

    public RestTemplate restTemplate(){
        return new RestTemplate();
    }

}
