package telran.spring.security;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.test.context.TestPropertySource;

import lombok.RequiredArgsConstructor;
import telran.spring.exceptions.NotFoundException;
import telran.spring.security.dto.Account;

@SpringBootTest(classes= {AccountProviderImpl.class, AccountingConfiguration.class, AccountServiceImpl.class, PasswordEncoder.class})
@TestPropertySource(properties = {"app.security.admin.password=ppp", "app.security.admin.username=admin"})

@RequiredArgsConstructor

class AccountingSeviceTest {
	@Autowired
UserDetailsManager userDetailsManager;
	
	@Autowired
	AccountServiceImpl accountService;
	PasswordValidator passwordValidator;
	PasswordEncoder passwordEncoder;
		
		
	@Test
	void adminExsistTest() {
		assertTrue(userDetailsManager.userExists("admin"));
	}
	
	@Test
	void addAccountTest() {
		String[] roles = {"USER"};
		Account accountAded = new Account("USER", "user1234", roles);
		accountService.addAccount(accountAded);
		accountAded = this.accountService.getAccount("USER");
		Account expected = new Account("USER", "user1234", roles);
		assertEquals(accountAded.getUsername(), expected.getUsername());
		assertEquals(accountAded.getRoles(), expected.getRoles());
		//assertTrue(passwordEncoder.matches(expected.getPassword(), account.getPassword()));
		assertThrowsExactly(IllegalStateException.class, () -> accountService.addAccount(expected));
		accountService.deleteAccount("USER");
		
	}
	
	@Test
	void deleteAccountTest() {
		String[] roles = {"USER"};
		Account accountAdded = new Account("USER", "user1234", roles);
		accountService.addAccount(accountAdded);
		accountService.deleteAccount("USER");
		assertThrowsExactly(IllegalArgumentException.class, () -> accountService.deleteAccount("USER"));
	}
	
	@Test
	void getAccountTest() {
		assertThrowsExactly(NotFoundException.class, () -> accountService.getAccount("USER"));
		String[] roles = {"USER"};
		accountService.addAccount(new Account("USER", "user1234", roles));
		assertEquals(accountService.getAccount("USER").getRoles(), roles);
		accountService.deleteAccount("USER");
	}
//	@Test
//	@Disabled
//	void updatePasswordTest() {
//		String[] roles = {"USER"};
//		Account accountAdded = new Account("USER", "user1234", roles);
//		String newPassword = "user12345";
//		accountService.addAccount(accountAdded);
//		accountService.updatePassword(accountAdded.getUsername(), newPassword);
//		boolean res = passwordEncoder.matches(newPassword, accountService.getAccount(accountAdded.getUsername()).getPassword());
//	}
		
	
}
