package com.example.popin.domain.MPg_Provider.dto;

import com.example.popin.domain.MPg_Provider.entity.ProviderProfile;

public class ProviderProfileDto {
    public Long id;
    public String userEmail;
    public String name;
    public String phone;
    public String businessRegistrationNumber;
    public boolean verified;
    public String bankName;
    public String accountNumber;
    public String accountHolder;

    public static ProviderProfileDto from(ProviderProfile p) {
        ProviderProfileDto d = new ProviderProfileDto();
        d.id = p.getId();
        d.userEmail = p.getUserEmail();
        d.name = p.getName();
        d.phone = p.getPhone();
        d.businessRegistrationNumber = p.getBusinessRegistrationNumber();
        d.verified = p.isVerified();
        d.bankName = p.getBankName();
        d.accountNumber = p.getAccountNumber();
        d.accountHolder = p.getAccountHolder();
        return d;
    }

    public void applyTo(ProviderProfile p) {
        if (this.name != null) p.setName(this.name);
        if (this.phone != null) p.setPhone(this.phone);
        if (this.businessRegistrationNumber != null) p.setBusinessRegistrationNumber(this.businessRegistrationNumber);
        if (this.bankName != null) p.setBankName(this.bankName);
        if (this.accountNumber != null) p.setAccountNumber(this.accountNumber);
        if (this.accountHolder != null) p.setAccountHolder(this.accountHolder);
        // verified는 운영 승인 로직에서만 변경한다면 여기서는 건드리지 않음
    }
}
