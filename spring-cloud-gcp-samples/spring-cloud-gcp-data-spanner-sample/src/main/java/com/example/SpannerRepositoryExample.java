/*
 *  Copyright 2018 original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.example;

import java.util.List;

import com.google.cloud.spanner.Key;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.gcp.data.spanner.core.SpannerOperations;
import org.springframework.cloud.gcp.data.spanner.repository.config.EnableSpannerRepositories;
import org.springframework.context.annotation.Bean;

/**
 * @author Chengyuan Zhao
 * @author Balint Pato
 */
@SpringBootApplication
@EnableSpannerRepositories(namedQueriesLocation = "classpath:spanner-named-queries.properties")
public class SpannerRepositoryExample {

	@Autowired
	SpannerOperations spannerOperations;

	@Autowired
	TradeRepository tradeRepository;

	public static void main(String[] args) {
		new SpringApplicationBuilder(SpannerRepositoryExample.class)
				.web(WebApplicationType.NONE)
				.run(args);
	}

	@Bean
	public CommandLineRunner commandLineRunner() {
		return args -> {
			this.tradeRepository.deleteAll();

			String[] actions = new String[] { "BUY", "SELL" };

			String[] stocks = new String[] { "stock1", "stock2", "stock3", "stock4",
					"stock5" };

			String traderId = "demo_trader";
			for (String stock : stocks) {
				for (String action : actions) {
					Trade t = new Trade();
					t.symbol = stock;
					t.action = action;
					t.traderId = traderId;
					t.price = 100.0;
					t.shares = 12345.6;
					this.spannerOperations.insert(t);
				}
			}

			System.out.println("The table for trades has been cleared and "
					+ this.tradeRepository.count() + " new trades have been inserted:");

			List<Trade> allTrades = this.spannerOperations.findAll(Trade.class);
			allTrades.forEach(System.out::println);

			System.out.println("There are " + this.tradeRepository.countByAction("BUY")
					+ " BUY trades: ");

			this.tradeRepository.findByAction("BUY").forEach(System.out::println);

			System.out.println("These are the Spanner primary keys for the trades:");

			allTrades.stream()
					.map(trade -> this.spannerOperations.getId(trade))
					.forEach(System.out::println);

			this.tradeRepository.deleteById(
					Key.newBuilder().append(stocks[0]).append(actions[0]).build());

			System.exit(0);
		};
	}

}