package rest;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class RessourcesTest {

	private Ressources res;

	@Before
	public void init(){
		res = new Ressources();
	}
	@After
	public void after(){
	
	}
	
	@Test
	public void testRessource(){
		assertNotNull(res);
	}
	
	@Test
	public void testAuthentifiedWithGoodUsernamePassword() throws IOException{
		res.connexion("azerty", "azerty");	
		assertTrue(res.isAuthentified());
		
	}
	
	@Test
	public void testAuthentifiedWithWrongPassword() throws IOException{
		res.connexion("azerty", "wrongPass");
		assertFalse(res.isAuthentified());
		assertEquals("<h3>Erreur d'authentification</h3>", res.connexion("azerty", "wrongPass"));
		
	}
}
