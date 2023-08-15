package telran.spring;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.ObjectMapper;

import telran.spring.exceptions.NotFoundException;
import telran.spring.security.AccountService;
import telran.spring.security.PasswordValidatorImpl;
import telran.spring.security.dto.Account;

@SpringBootApplication

class AccountServiceMock implements AccountService {

	 static final String NOT_EXISTED_USERNAME = "user_not_exist";

	@Override
	public Account getAccount(String username) {
		
		return new Account(username, username, new String[] {"ADMIN"});
	}

	@Override
	public void addAccount(Account account) {
		
	}

	@Override
	public void updatePassword(String username, String newPassword) {
		if (username.equals(NOT_EXISTED_USERNAME)) {
			throw new NotFoundException(username);
		}
		
	}

	@Override
	public void deleteAccount(String username) {
		if (username.equals(NOT_EXISTED_USERNAME)) {
			throw new NotFoundException(username);
		}
		
	}
	
}

@WebMvcTest({AccountController.class, PasswordValidatorImpl.class, AccountServiceMock.class, SecurityConfiguration.class})
@WithMockUser(password = "ddd", username = "admin", roles = {"ADMIN"})
public class AccountingControllerTest {
	@Autowired
	MockMvc mockMvc;
	@Autowired
	AccountController controller;
	Account account = new Account("user123", "userPass1", new String[] {"ADMIN", "USER"});
	Account accountWrongUsername = new Account("user", "userPass1", new String[] {"ADMIN", "USER"});
	Account accountWrongRoles = new Account("user123", "userPass1", new String[] {});
	Account accountWrongPassword = new Account("user123", "userPass", new String[] {"ADMIN", "USER"});
	@Autowired
	ObjectMapper mapper;
	String baseUrl = "http://localhost:8080/accounts";
     @Test
     void loadContext() {
    	 assertNotNull(mockMvc);
    	 assertNotNull(controller);
     }
     @Test
     void getAccountTest() throws Exception {
    	 mockMvc.perform(get(baseUrl + "/user")).andDo(print())
    			 .andExpect(status().isOk());
     }
     @Test
     void addAccountNormalFlowTest() throws Exception {
    	 String accountJson = mapper.writeValueAsString(account);
    	 var actions = getRequestBase(accountJson);
    	 actions.andExpect(status().isOk());
    	 
     }
     @Test
     void addAccountUsernameWrongFlowTest() throws Exception {
    	 String accountJson = mapper.writeValueAsString(accountWrongUsername);
    	 var actions = getRequestBase(accountJson);
    	 actions.andExpect(status().isBadRequest());
    	 
     }
     @Test
     void addAccountRolesWrongFlowTest() throws Exception {
    	 String accountJson = mapper.writeValueAsString(accountWrongRoles);
    	 var actions = getRequestBase(accountJson);
    	 actions.andExpect(status().isBadRequest());
    	 
     }
     @Test
     void addAccountPasswordWrongFlowTest() throws Exception {
    	 String accountJson = mapper.writeValueAsString(accountWrongPassword);
    	 var actions = getRequestBase(accountJson);
    	 actions.andExpect(status().isBadRequest());
    	 
     }
     @Test
     void updatePasswordNormalFlowTest() throws Exception {
    	 var actions = getRequestBasePut("user123", "userPass2");
    	 actions.andExpect(status().isOk());
    	 
     }
     @Test
     void updatePasswordWrongPasswordFlowTest() throws Exception {
    	 var actions = getRequestBasePut("user123", "userPass");
    	 actions.andExpect(status().isBadRequest());
    	 
     }
     @Test
     void updatePasswordNotExistsFlowTest() throws Exception {
    	 var actions = getRequestBasePut(AccountServiceMock.NOT_EXISTED_USERNAME, "userPass1");
    	 actions.andExpect(status().isNotFound());
    	 
     }
     @Test
     void deleteAccountNormalFlowTest() throws Exception {
    	 var actions = getRequestBaseDelete("usewr123");
    	 actions.andExpect(status().isOk());
     }
     @Test
     void deleteAccountNotExistsFlowTest() throws Exception {
    	 var actions = getRequestBaseDelete(AccountServiceMock.NOT_EXISTED_USERNAME);
    	 actions.andExpect(status().isNotFound());
     }
     private ResultActions getRequestBase(String json) throws Exception {
 		return mockMvc.perform(post(baseUrl).contentType(MediaType.APPLICATION_JSON).content(json))
 		.andDo(print());
 	}
     private ResultActions getRequestBasePut(String username, String json) throws Exception {
  		return mockMvc.perform(put(baseUrl+ "/" + username).contentType(MediaType.APPLICATION_JSON).content(json))
  		.andDo(print());
  	}
     private ResultActions getRequestBaseDelete(String username) throws Exception {
   		return mockMvc.perform(delete(baseUrl + "/" + username))
   		.andDo(print());
   	}
     
     
     
}