package telran.spring.security;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import telran.spring.exceptions.NotFoundException;
import telran.spring.security.dto.Account;
@Service
@RequiredArgsConstructor
@Slf4j
public class AccountServiceImpl implements AccountService {
	 final PasswordEncoder passwordEncoder;
     final AccountProvider provider;
     ConcurrentHashMap<String, Account> accounts = new ConcurrentHashMap<>();
     @Value("${app.expiration.period:600}")
     long expirationPeriod;
     @Value("${app.security.passwords.limit:3}")
		int limitPasswords;
     @Autowired
      UserDetailsManager userDetailsManager;
     @Value("${app.security.validation.period:3600000}")
	private long validationPeriod;
     @Value("${app.security.validation.time.unit:HOURS}")
     private ChronoUnit unit;
     
     
     
	@Override
	public Account getAccount(String username) {
		Account res = accounts.get(username);
		if (res == null) {
			throw new NotFoundException(username + " not found");
		}
		return res;
	}

	@Override
	public void addAccount(Account account) {
		String username = account.getUsername();
		if (userDetailsManager.userExists(username)) {
			throw new IllegalStateException(String.format("user %s already exists", username));
		}
		if(accounts.containsKey(username)) {
			throw new RuntimeException("error of synchronization between accounts and accounts manager");
		}
		
		String plainPassword = account.getPassword();
		String passwordHash = passwordEncoder.encode(plainPassword);
		Account user = new Account(username, passwordHash, account.getRoles());
		
		LocalDateTime ldt = getExpired();
		user.setExpDate(ldt);
		
		LinkedList<String> passwords = new LinkedList<>();
		passwords.add(passwordHash);
		user.setPasswords(passwords);
		
		
		createUser(user);
		log.debug("created user {}", username);
		
	}

	private LocalDateTime getExpired() {
		return LocalDateTime.now().plus(expirationPeriod, unit);
	}

	private void createUser(Account user) {
		accounts.putIfAbsent(user.getUsername(), user) ;
		userDetailsManager.createUser(createUserDetails(user));
		
	}

	@Override
	public void updatePassword(String username, String newPassword) {
		Account account = accounts.get(username);
		if (account == null) {
			throw new NotFoundException(username + " not found");
		}
		updateUser(account, newPassword);
		log.debug("use {} updated", username);

	}

	private void updateUser(Account account, String newPassword) {
		if (account.getPasswords().stream()
				.anyMatch(hash -> passwordEncoder.matches(newPassword, hash))) {
			throw new IllegalStateException("mismatches Passwords Strategy");
		}
		LinkedList<String> passwords = account.getPasswords();
		String hashPassword = passwordEncoder.encode(newPassword);
		
		if (passwords.size() == limitPasswords) {
			passwords.removeFirst();
		}
		passwords.add(hashPassword);
		String username = account.getUsername();
		String[]roles = account.getRoles();
		Account newAccount = new Account(username, hashPassword, roles);
		newAccount.setPasswords(passwords);
		LocalDateTime expTime = getExpired();
		newAccount.setExpDate(expTime);
		accounts.put(username, newAccount);
		userDetailsManager.updateUser(createUserDetails(newAccount));
		
	}

	private UserDetails createUserDetails(Account account) {
		return User.withUsername(account.getUsername())
				.password(account.getPassword()).roles(account.getRoles())
				.accountExpired(isExpired(account)).build();
	}

	@Override
	public void deleteAccount(String username) {
		Account account = accounts.remove(username);
		if (account == null) {
			throw new NotFoundException(username + " not found");
		}
		userDetailsManager.deleteUser(username);
		log.debug("user {} deleted", username);

	}
	@PostConstruct
	void restoreAccounts() {
		Thread thread = new Thread(() -> {
			while(true) {
			
				try {
					Thread.sleep(validationPeriod);
				} catch (InterruptedException e) {
					
				}
				expirationValidation();
			}
		});
		thread.setDaemon(true);
		thread.start();
		List<Account> listAccounts = provider.getAccounts();
		listAccounts.forEach(a -> createUser(a));
	}
	private void expirationValidation() {
		
		int[] count = {0};
		accounts.values().stream().filter(this::isExpired).forEach(a -> {
			log.debug("account {} expired", a);
				userDetailsManager.updateUser(User.withUsername(a.getUsername()).password(a.getPassword())
						.accountExpired(true).build());
				count[0]++;
		});
		log.debug("expiration validation {} accounts have been expired", count[0]);	
		
		
	}

	private boolean isExpired(Account a) {
		
		return LocalDateTime.now().isAfter(a.getExpDate());
	}

	@PreDestroy
	void saveAccounts() {
		provider.setAccounts(new LinkedList<>(accounts.values()));
	}
	
	

}