/**
 * Created by Luzius Meisser on Jun 18, 2017
 * Copyright: Meisser Economics AG, Zurich
 * Contact: luzius@meissereconomics.com
 *
 * Feel free to reuse this code under the MIT License
 * https://opensource.org/licenses/MIT
 */
package com.agentecon.exercises;

import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.agentecon.IAgentFactory;
import com.agentecon.classloader.GitSimulationHandle;
import com.agentecon.classloader.LocalSimulationHandle;
import com.agentecon.configuration.AgentFactoryMultiplex;
import com.agentecon.configuration.CompilingAgentFactory;

public class ExerciseAgentLoader extends AgentFactoryMultiplex {

	private static final Collection<String> TEAMS = createRepos(15);

	public ExerciseAgentLoader(String classname) throws SocketTimeoutException, IOException {
		super(createFactories(classname));
	}

	private static Collection<String> createRepos(int count) {
		assert count < 100;
		String[] repos = new String[count];
		for (int i = 0; i < repos.length; i++) {
			String number = Integer.toString(i);
			repos[i] = "team" + (number.length() == 1 ? "00" : "0") + number;
		}
		return Arrays.asList(repos);
	}

	private static IAgentFactory[] createFactories(String classname) throws SocketTimeoutException, IOException {
		ArrayList<CompilingAgentFactory> factories = new ArrayList<>();
		try {
			CompilingAgentFactory defaultFactory = new CompilingAgentFactory(classname, "meisser", "course");
			factories.add(defaultFactory);
		} catch (IOException e) {
			System.out.println("Cannot load agents from github.com/meisser/course due to: " + e);
		}
		Stream<CompilingAgentFactory> stream = TEAMS.parallelStream().map(team -> {
			try {
				return new CompilingAgentFactory(classname, new GitSimulationHandle("meisser", team));
			} catch (IOException e) {
				return null;
			}
		}).filter(factory -> factory != null);
		factories.addAll(stream.collect(Collectors.toList()));
		LocalSimulationHandle local = new LocalSimulationHandle(new File("../exercises/src"));
		factories.add(new CompilingAgentFactory(classname, local));
		return factories.toArray(new IAgentFactory[factories.size()]);
	}

}
