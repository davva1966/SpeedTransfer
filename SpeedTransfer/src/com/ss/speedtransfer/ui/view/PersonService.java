package com.ss.speedtransfer.ui.view;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.ss.speedtransfer.ui.view.Person.Gender;

public class PersonService {
	static String[] maleNames = { "Bart", "Homer", "Lenny", "Carl", "Waylon", "Ned", "Timothy" };
	static String[] femaleNames = { "Marge", "Lisa", "Maggie", "Edna", "Helen", "Jessica" };
	static String[] lastNames = { "Simpson", "Leonard", "Carlson", "Smithers", "Flanders", "Krabappel", "Lovejoy" };

	/**
	 * Creates a list of {@link Person}s.
	 * 
	 * @param numberOfPersons
	 *            The number of {@link Person}s that should be generated.
	 * @return
	 */

	public static List<Person> getPersons(int numberOfPersons) {
		List<Person> result = new ArrayList<Person>();

		for (int i = 0; i < numberOfPersons; i++) {
			result.add(createPerson(i));
		}

		return result;
	}

	/**
	 * Creates a random person out of names which are taken from "The Simpsons"
	 * and enrich them with random generated married state and birthday date.
	 * 
	 * @return
	 */

	private static Person createPerson(int id) {
		Random randomGenerator = new Random();

		Person result = new Person(id);
		result.setGender(Gender.values()[randomGenerator.nextInt(2)]);

		if (result.getGender().equals(Gender.MALE)) {
			result.setFirstName(maleNames[randomGenerator.nextInt(maleNames.length)]);
		} else {
			result.setFirstName(femaleNames[randomGenerator.nextInt(femaleNames.length)]);
		}

		result.setLastName(lastNames[randomGenerator.nextInt(lastNames.length)]);
		result.setMarried(randomGenerator.nextBoolean());

		int month = randomGenerator.nextInt(12);
		int day = 0;
		if (month == 2) {
			day = randomGenerator.nextInt(28);
		} else {
			day = randomGenerator.nextInt(30);
		}
		int year = 1920 + randomGenerator.nextInt(90);

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		try {
			result.setBirthday(sdf.parse("" + year + "-" + month + "-" + day));
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return result;
	}

}
