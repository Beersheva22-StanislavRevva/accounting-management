package telran.spring;

import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import telran.spring.security.AccountService;
import telran.spring.security.PasswordValidator;
import telran.spring.security.dto.Account;
@RestController
@RequestMapping("accounts")
@RequiredArgsConstructor

public class AccountController {
final AccountService accountService;
final PasswordValidator passwordValidator;
	@GetMapping("{username}")
	public Account getAccount(@PathVariable String username) {
		
		return accountService.getAccount(username);
	}

	@PostMapping
	public void addAccount(@RequestBody @Valid Account account) {
		passwordValidator.validate(account.getPassword());
		accountService.addAccount(account);

	}

	@PutMapping("{username}")
	public void updatePassword(@PathVariable String username, @RequestBody String newPassword) {
		passwordValidator.validate(newPassword);
		accountService.updatePassword(username, newPassword);

	}

	@DeleteMapping("{username}")
	public void deleteAccount(@PathVariable String username) {
		accountService.deleteAccount(username);

	}

}