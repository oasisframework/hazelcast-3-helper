package io.github.oasisframework.hazelcast.three.helper.checker;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HzHealthChecker {

	private static boolean isAlive = true;

	public static void setIsAlive(boolean isAlive) {
		HzHealthChecker.isAlive = isAlive;
	}
}
