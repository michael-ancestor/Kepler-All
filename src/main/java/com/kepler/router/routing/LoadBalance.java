package com.kepler.router.routing;

import java.util.Iterator;
import java.util.List;

import com.kepler.KeplerRoutingException;
import com.kepler.host.Host;
import com.kepler.protocol.Request;
import com.kepler.router.Routing;

/**
 * @author zhangjiehao 2015年9月9日
 */
abstract class LoadBalance implements Routing {

	abstract protected int next(int weights);

	@Override
	public Host route(Request request, List<Host> hosts) {
		// 如果仅一台主机则立即返回
		if (hosts.size() == 1) {
			return hosts.get(0);
		}
		Slots slots = new Slots(hosts);
		int cursor = this.next(this.sumUpWeight(hosts));
		while (slots.hasNext()) {
			if (cursor < slots.next()) {
				return slots.host();
			}
		}
		throw new KeplerRoutingException("None right service for " + request.service());
	}

	private int sumUpWeight(List<Host> hosts) {
		int totalWeight = 0;
		if (!hosts.isEmpty()) {
			for (Host host : hosts) {
				totalWeight += host.priority();
			}
		}
		return totalWeight;
	}

	private static class Slots implements Iterator<Integer> {

		private List<Host> hosts;

		private int currentHost;

		private int currentSlot;

		private Slots(List<Host> hosts) {
			this.hosts = hosts;
			this.currentHost = -1;
			this.currentSlot = 0;
		}

		@Override
		public boolean hasNext() {
			return this.currentHost < this.hosts.size() - 1;
		}

		@Override
		public Integer next() {
			this.currentHost += 1;
			this.currentSlot += this.hosts.get(this.currentHost).priority();
			return this.currentSlot;
		}

		public Host host() {
			return this.hosts.get(this.currentHost);
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
}
