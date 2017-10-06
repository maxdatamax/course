package com.agentecon.configuration;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;

import com.agentecon.ISimulation;
import com.agentecon.classloader.CompilingClassLoader;
import com.agentecon.classloader.RemoteLoader;
import com.agentecon.goods.Good;
import com.agentecon.research.IInnovation;
import com.agentecon.sim.Event;
import com.agentecon.sim.SimulationConfig;

public class CustomConfiguration extends SimulationConfig {
	
	private SimulationConfig delegate;
	
	public CustomConfiguration() throws IOException {
		this("com.agentecon.exercise3.MoneyConfiguration");
	}
	
	public CustomConfiguration(String classname) throws IOException {
		this((RemoteLoader) CustomConfiguration.class.getClassLoader(), classname);
	}
	
	public CustomConfiguration(RemoteLoader parent, String className) throws IOException {
		super(10000);
		try {
			// use same source as parten
			CompilingClassLoader loader = new CompilingClassLoader(parent, parent.getSource().copy(false));
			delegate = (SimulationConfig) loader.loadClass(className).newInstance();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Good getMoney(){
		return delegate.getMoney();
	}

	@Override
	public long getSeed() {
		return delegate.getSeed();
	}

	@Override
	public Collection<Event> getEvents() {
		return delegate.getEvents();
	}

	@Override
	public void addEvent(Event e) {
		delegate.addEvent(e);
	}

	@Override
	public int getRounds() {
		return delegate.getRounds();
	}

	@Override
	public int getIntradayIterations() {
		return delegate.getIntradayIterations();
	}

	@Override
	public IInnovation getInnovation() {
		return delegate.getInnovation();
	}
	
	@Override
	public String getName(){
		return delegate.getName();
	}

	@Override
	public void diagnoseResult(PrintStream out, ISimulation stats) {
		delegate.diagnoseResult(out, stats);
	}

	@Override
	public double getCurrentDiscountRate() {
		return delegate.getCurrentDiscountRate();
	}

}