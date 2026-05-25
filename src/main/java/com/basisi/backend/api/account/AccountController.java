package com.basisi.backend.api.account;

import com.basisi.backend.api.account.dto.AccountMeResponse;
import com.basisi.backend.api.account.dto.UpdateEmailRequest;
import com.basisi.backend.api.account.dto.UpdateNameRequest;
import com.basisi.backend.api.account.dto.UpdatePasswordRequest;
import com.basisi.backend.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/account")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/me")
    public ResponseEntity<AccountMeResponse> me() {
        return ResponseEntity.ok(accountService.getMe());
    }

    @PutMapping("/name")
    public ResponseEntity<AccountMeResponse> updateName(@Valid @RequestBody UpdateNameRequest request) {
        return ResponseEntity.ok(accountService.updateName(request));
    }

    @PutMapping("/email")
    public ResponseEntity<AccountMeResponse> updateEmail(@Valid @RequestBody UpdateEmailRequest request) {
        return ResponseEntity.ok(accountService.updateEmail(request));
    }

    @PutMapping("/password")
    public ResponseEntity<Void> updatePassword(@Valid @RequestBody UpdatePasswordRequest request) {
        accountService.updatePassword(request);
        return ResponseEntity.noContent().build();
    }
}

