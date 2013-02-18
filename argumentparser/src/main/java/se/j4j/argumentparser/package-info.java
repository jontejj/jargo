/**
 * TODO: Write readme here
 * Oneliner API description
 * 
 * <pre>
 * Flesh out examples... with pretty-printing
 * 
 * <h2>Localization</h2>
 * By default, {@link se.j4j.argumentparser.ArgumentFactory#integerArgument(String...)}, {@link se.j4j.argumentparser.ArgumentFactory#longArgument(String...)} and so on,
 * formats numbers with {@link se.j4j.strings.Describers#numberDescriber()} in a {@link java.util.Locale} sensitive way. If this isn't what you want you can override this with
 * {@link se.j4j.argumentparser.ArgumentBuilder#defaultValueDescriber(se.j4j.strings.Describer) defaultValueDescriber(Describers.toStringDescriber()}.
 * 
 * <h2>API compatibility notes</h2>
 * Public methods that have Guava types in their method signature such as:
 * {@link se.j4j.argumentparser.ArgumentBuilder#limitTo(com.google.common.base.Predicate)}
 * {@link se.j4j.argumentparser.ArgumentBuilder#defaultValueSupplier(com.google.common.base.Supplier)}
 * {@link se.j4j.argumentparser.StringParsers#asFunction(StringParser)}
 * May be changed when JDK 8 is here as it will contain those interfaces without the need of Guava. For now they are marked with {@link com.google.common.annotations.Beta}
 * </pre>
 */
@ParametersAreNonnullByDefault
package se.j4j.argumentparser;

import javax.annotation.ParametersAreNonnullByDefault;

