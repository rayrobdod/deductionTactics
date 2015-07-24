package com.rayrobdod.deductionTactics;

import org.openjdk.jmh.annotations.*;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(value = 3, jvmArgsAppend = {"-server", "-disablesystemassertions"})
public class DirectionWeaknessMultiplerBenchmark {
	@State(Scope.Thread)
	static public class AdditionState {
		int x;
		int y;
		
		@Setup(Level.Iteration)
		public void prepare() {
			Random random = new Random();
			x = random.nextInt();
			y = random.nextInt();
		}
		
		@TearDown(Level.Iteration)
		public void shutdown() {
			x = y = 0; // useless in this benchmark...
		}
	}
	
	@Benchmark
	@Warmup(iterations = 10, time = 3, timeUnit = TimeUnit.SECONDS)
	public int _baseline(AdditionState state) {
		return state.x;
	}
	
	@Benchmark
	@Warmup(iterations = 5, time = 5, timeUnit = TimeUnit.SECONDS)
	public double angle(AdditionState state) {
		double theta = Math.abs(Math.atan2(state.x, state.y));
		double x = 1 - (theta / Math.PI);
		return (1 * x * x) + (.5 *  x) + .5;
	}
	
	@Benchmark
	@Warmup(iterations = 5, time = 5, timeUnit = TimeUnit.SECONDS)
	public double angle2(AdditionState state) {
		double theta = Math.abs(Math.atan2(state.x, state.y));
		double x = 1 - (theta / Math.PI);
		return ((x + .5) * x) + .5;
	}
	
	@Benchmark
	@Warmup(iterations = 5, time = 5, timeUnit = TimeUnit.SECONDS)
	public double angle3(AdditionState state) {
		double theta = 1 - Math.abs(Math.atan2(state.x, state.y)) / Math.PI;
		double theta8 = theta * 8;
		int theta8i = (int) theta;
		return (((theta8i + 4) * theta8i) + 32) / 64f;
	}
	
	@Benchmark
	@Warmup(iterations = 5, time = 5, timeUnit = TimeUnit.SECONDS)
	public double vector(AdditionState state) {
		double unit = state.x / Math.hypot(state.x, state.y);
		double x = (unit - 1) / 2 + 1;
		return (1 * x * x) + (.5 *  x) + .5;
	}
	
	@Benchmark
	@Warmup(iterations = 5, time = 5, timeUnit = TimeUnit.SECONDS)
	public double vector2(AdditionState state) {
		double unit = state.x / Math.hypot(state.x, state.y);
		double x = (unit + 1) / 2;
		return (1 * x * x) + (.5 *  x) + .5;
	}
	
	@Benchmark
	@Warmup(iterations = 5, time = 5, timeUnit = TimeUnit.SECONDS)
	public double linear2(AdditionState state) {
		int x = state.x;
		int y = state.y;
		int dividend = Math.max(1, Math.max(Math.abs(x), Math.abs(y * 4)));
		int prescale = x * 16 / dividend;
		return ((0.25 / 256) * prescale + (1.5/32)) * prescale + 1;
	}
	
	@Benchmark
	@Warmup(iterations = 5, time = 5, timeUnit = TimeUnit.SECONDS)
	public double linear4(AdditionState state) {
		int x = state.x;
		int y = state.y;
		int dividend = Math.max(1, Math.max(Math.abs(x), Math.abs(y * 4)));
		int prescale = x * 16 / dividend;
		double postscale = ((((-.00002848307291667) * prescale + -.0001953125) * prescale + .008268291666667) * prescale + .096875) * prescale + 1;
		return Math.rint(postscale * 128) / 128;
	}
	
	@Benchmark
	@Warmup(iterations = 5, time = 5, timeUnit = TimeUnit.SECONDS)
	public double linear2pair(AdditionState state) {
		int x = state.x;
		int y = state.y;
		int dividend = Math.max(1, Math.max(Math.abs(x), Math.abs(y * 4)));
		int prescale = x * 16 / dividend;
		if (x >= 0) {
			return (-1.0 / 192 * prescale + (7.0 / 48)) * prescale + 1;
		} else {
			return (1.0 / 384 * prescale + (7.0 / 96)) * prescale + 1;
		}
	}
}
