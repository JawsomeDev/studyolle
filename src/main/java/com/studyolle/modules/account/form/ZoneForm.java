package com.studyolle.modules.account.form;


import com.studyolle.modules.zone.Zone;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ZoneForm {


    @NotNull
    private String zoneName;

    public String getCityName(){
        return zoneName.substring(0, zoneName.indexOf("("));
    }

    public String getProvinceName(){

        return zoneName.substring(zoneName.indexOf("/") + 1);
    }

    public String getLocalNameOfCity(){
        return zoneName.substring(zoneName.indexOf("(") + 1, zoneName.indexOf(")"));
    }

    public Zone getZone(){
        return Zone.builder()
                .city(this.getCityName())
                .localNameOfCity(this.getLocalNameOfCity())
                .province(this.getProvinceName()).build();
    }
}



