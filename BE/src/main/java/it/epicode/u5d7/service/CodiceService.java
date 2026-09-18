package it.epicode.u5d7.service;

import java.security.SecureRandom;
import java.util.HexFormat;

import org.springframework.stereotype.Service;

/** Unico punto in cui si generano valori random "segreti": usa SecureRandom. */
@Service
public class CodiceService {

	private final SecureRandom random = new SecureRandom();

	/** Codice numerico fra 100000 e 999999: 6 cifre senza zeri iniziali. */
	public int codice6Cifre() {
		return 100000 + random.nextInt(900000);
	}

	/** 16 byte random codificati in esadecimale = stringa di 32 caratteri. */
	public String token32() {
		byte[] bytes = new byte[16];
		random.nextBytes(bytes);
		return HexFormat.of().formatHex(bytes);
	}
}
