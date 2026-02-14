package com.cvd.springboot.service;

import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {
    
    private final Map<String, String> otpStore = new ConcurrentHashMap<>();
    
    public String generateOtp(String mobile) {
        String otp = String.valueOf(100000 + new Random().nextInt(900000));
        otpStore.put(mobile, otp);
        System.out.println("✅ OTP GENERATED for " + mobile + " is: " + otp);
        return otp;
    }
    
    public boolean verifyOtp(String mobile, String otp) {
        System.out.println("🔍 VERIFY - Mobile: [" + mobile + "], OTP: [" + otp + "]");
        
        String storedOtp = otpStore.get(mobile);
        
        if (storedOtp == null) {
            System.out.println("❌ No OTP found for mobile: " + mobile);
            return false;
        }
        
        // Normalize both to strings and trim whitespace
        String normalizedStored = storedOtp.trim();
        String normalizedReceived = (otp != null) ? otp.trim() : "";
        
        System.out.println("💾 Stored: [" + normalizedStored + "], Received: [" + normalizedReceived + "]");
        
        if (normalizedStored.equals(normalizedReceived)) {
            otpStore.remove(mobile);
            System.out.println("✅ OTP VERIFIED!");
            return true;
        }
        
        System.out.println("❌ OTP MISMATCH");
        return false;
    }
}