/**
 * Created by Luzius Meisser on Jun 18, 2017
 * Copyright: Meisser Economics AG, Zurich
 * Contact: luzius@meissereconomics.com
 *
 * Feel free to reuse this code under the MIT License
 * https://opensource.org/licenses/MIT
 */
package com.agentecon.exercise9;

import com.agentecon.agent.Endowment;
import com.agentecon.agent.IAgentIdGenerator;
import com.agentecon.finance.Producer;
import com.agentecon.firm.InputFactor;
import com.agentecon.firm.sensor.SensorInputFactor;
import com.agentecon.goods.Good;
import com.agentecon.goods.IStock;
import com.agentecon.goods.Quantity;
import com.agentecon.learning.ExpSearchBelief;
import com.agentecon.market.Ask;
import com.agentecon.market.Bid;
import com.agentecon.market.IPriceMakerMarket;
import com.agentecon.market.Price;
import com.agentecon.production.IPriceProvider;
import com.agentecon.production.IProductionFunction;
import com.agentecon.production.PriceUnknownException;

/**
 * There are only four real estate agent in the simulation, two for each team.
 * 
 * They start with an inventory of 1000 taler and 10 units of land.
 * 
 * All four real estate agents share the same production function, and the production function has a memory! So production gets harder and harder with every function call...
 */
public class RealEstateAgent extends Producer {

	private static final double DISTRIBUTION_RATIO = 0.02;

	private double minCashLevel;
	private InputFactor input;

	private Price landPrice;

	public RealEstateAgent(IAgentIdGenerator id, Endowment end, IProductionFunction prodFun) {
		super(id, end, prodFun);

		assert prodFun.getInputs().length == 1;
		Good manhour = prodFun.getInputs()[0];
		this.input = new SensorInputFactor(getInventory().getStock(manhour), new ExpSearchBelief(10));

		// hard-coded land price. Maybe something dynamic works better?
		this.landPrice = new Price(prodFun.getOutput(), 100);

		this.minCashLevel = getMoney().getAmount() / 2;
	}

	private IStock getLand() {
		return getInventory().getStock(landPrice.getGood());
	}

	// calculate the value of our inventory (money, land, man-hours)
	private double calculateCapital() {
		return getInventory().calculateValue(new IPriceProvider() {

			@Override
			public double getPriceBelief(Good good) throws PriceUnknownException {
				if (input.getGood().equals(good)) {
					return input.getPrice();
				} else if (landPrice.getGood().equals(good)) {
					return landPrice.getPrice();
				} else {
					throw new PriceUnknownException();
				}
			}
		});
	}

	@Override
	public void offer(IPriceMakerMarket market) {
		IStock money = getMoney();
		IStock land = getLand();
		if (money.getAmount() > 100) {
			// if we have a minimal amount of money, we bid for land others might want to sell
			double landBudget = (money.getAmount() - 100) / 10;
			Price bidPrice = new Price(landPrice.getGood(), landPrice.getPrice() * 0.9);
			market.offer(new Bid(this, money, land, bidPrice, bidPrice.getAmountAt(landBudget)));
		}
		if (land.hasSome()) {
			// try to sell the land we have 10% above our guess for the right land price
			Price askPrice = new Price(landPrice.getGood(), landPrice.getPrice() * 1.1);
			market.offer(new Ask(this, money, land, askPrice, land.getAmount()));
		}

		// buy some man-hours to produce additional land
		if (getMoney().getAmount() > minCashLevel) {
			if (shouldProduce()) {
				this.input.createOffers(market, this, getMoney(), (getMoney().getAmount() - minCashLevel) / 10);
			}
		}
	}

	/**
	 * Is it actually still worth producing anything at current prices?
	 */
	protected boolean shouldProduce() {
		double potentialInvestment = 1000;
		double manHourPrice = input.getPrice();
		Quantity manhours = new Quantity(input.getGood(), potentialInvestment / manHourPrice);
		IProductionFunction prodFun = getProductionFunction();
		Quantity landWeCouldProduce = prodFun.calculateOutput(manhours);
		double landValue = landWeCouldProduce.getAmount() * landPrice.getPrice();
		boolean shouldProduce = landValue > manHourPrice;
		return shouldProduce;
	}

	@Override
	public void adaptPrices() {
		input.adaptPrice();

		// market making already adapts prices during offer phase, no need to act here
	}

	@Override
	public void produce() {
		super.produce(); // just use all available man-hours to produce some land
	}

	@Override
	protected double calculateDividends(int day) {
		return getMoney().getAmount() * DISTRIBUTION_RATIO - minCashLevel;
	}

}
