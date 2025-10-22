package com.example.OnePieceTheoryEvaluator.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper(){
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                .setFieldMatchingEnabled(true)
                .setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE)
                .setMatchingStrategy(MatchingStrategies.STANDARD);
        
        // Configure mapping for Theory to TheoryDTO
        modelMapper.typeMap(com.example.OnePieceTheoryEvaluator.entity.Theory.class, 
                           com.example.OnePieceTheoryEvaluator.dto.TheoryDTO.class)
                .addMappings(mapper -> {
                    mapper.map(src -> src.getAiScore(), com.example.OnePieceTheoryEvaluator.dto.TheoryDTO::setAiScore);
                });
        
        return modelMapper;
    }

}