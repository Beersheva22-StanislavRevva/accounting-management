package telran.spring.security;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import telran.spring.security.dto.Account;

@Service
@Slf4j

public class AccountProviderImpl implements AccountProvider {
	
@Value("${app.security.accounts.file.name:accounts.data}")
	private String fileName;
		
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Account> getAccounts() {
		List<Account>res = Collections.emptyList();
		if (Files.exists(Path.of(fileName))) {
			try (ObjectInputStream stream = new ObjectInputStream(new FileInputStream(fileName))) {
				res = (List<Account>) stream.readObject();
				log.info("accounts have beeen restored from the file: " + fileName);
			} catch (Exception e) {
				throw new RuntimeException(String.format("eroorr %s during resoring from file %s", e.toString(), fileName));
			}
		}
		return res;
	}

	@Override
	public void setAccounts(List<Account> accounts) {
		try (ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(fileName))){
			stream.writeObject(accounts);
			log.info("{} accounts have beeen saved to the file {}", accounts.size(), fileName);
		} catch(Exception e) {
			throw new RuntimeException(String.format("error %s during saving accounts to file %s", e.toString(), fileName)); //some error
		}

	}

}
