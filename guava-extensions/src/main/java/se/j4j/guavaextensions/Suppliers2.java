package se.j4j.guavaextensions;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayListWithCapacity;

import java.util.List;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

/**
 * Additional implementations of the {@link Supplier} interface
 */
public final class Suppliers2
{
	private Suppliers2()
	{
	}

	/**
	 * Creates a {@link Supplier} that supplies {@code elementsToSupply} number of elements from
	 * {@code elementSupplier}
	 * 
	 * @throws IllegalArgumentException if {@code elementsToSupply} is less than zero
	 */
	public static <T> Supplier<List<T>> ofRepeatedElements(Supplier<? extends T> elementSupplier, int elementsToSupply)
	{
		checkNotNull(elementSupplier);
		checkArgument(elementsToSupply >= 0, "elementsToSupply may not be negative");
		return new ListSupplier<T>(elementSupplier, elementsToSupply);
	}

	private static final class ListSupplier<T> implements Supplier<List<T>>
	{
		private final Supplier<? extends T> elementSupplier;
		private final int elementsToSupply;

		private ListSupplier(Supplier<? extends T> elementSupplier, final int elementsToSupply)
		{
			this.elementSupplier = elementSupplier;
			this.elementsToSupply = elementsToSupply;
		}

		@Override
		public List<T> get()
		{
			List<T> result = newArrayListWithCapacity(elementsToSupply);
			for(int i = 0; i < elementsToSupply; i++)
			{
				result.add(elementSupplier.get());
			}
			return result;
		}
	}

	/**
	 * Returns true if {@code Supplier} is likely to supply values very fast
	 */
	public static boolean isSuppliedAlready(Supplier<?> supplier)
	{
		if(supplier.getClass().equals(OF_INSTANCE))
			return true;
		else if(supplier instanceof ListSupplier<?>)
		{
			ListSupplier<?> listSupplier = (ListSupplier<?>) supplier;
			if(listSupplier.elementSupplier.getClass().equals(OF_INSTANCE))
				return true;
		}
		return false;
	}

	private static final Class<?> OF_INSTANCE = Suppliers.ofInstance(null).getClass();
}
