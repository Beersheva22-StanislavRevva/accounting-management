package telran.spring;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import telran.spring.security.AccountService;
import telran.spring.security.AccountServiceImpl;
import telran.spring.security.PasswordValidator;
import telran.spring.security.dto.Account;

@RestController
@RequestMapping("acounts")
@RequiredArgsConstructor

public class AccountController implements AccountService {

final AccountServiceImpl accountService;
final PasswordValidator passwordValidator;


	@Override
	@GetMapping("{username}")
	public Account getAccount(@PathVariable String username) {
		return accountService.getAccount(username);
	}
	
	@Override
	@PostMapping
	public void addAccount(Account account) {
		accountService.addAccount(account);
	}
	
	@Override
	@PutMapping("{username}")
	public void updatePassword(@PathVariable String username, String newPassword) {
		accountService.updatePassword(username, newPassword);
	}

	@Override
	@DeleteMapping("{username}")
	public void deleteAccount(@PathVariable String username) {
		accountService.deleteAccount(username);
		
	}

}
