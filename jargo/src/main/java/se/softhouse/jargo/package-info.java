/* Copyright 2013 Jonatan Jönsson
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
/**
 * FIXME: Write readme here
 * Oneliner API description
 *
 * <h2>Inspiration (a.k.a Credits)</h2>
 * If you recognize any features in this API it may come from:
 * <b>git</b> - the command feature, suggestions for invalid arguments
 * <b>maven</b> - executing several commands
 * <b>jcommander</b> - almost everything else (this is what I based the feature set on)
 *
 * <h2>Glossary</h2>
 * Named Argument
 * Parameter
 * Option
 * Indexed Argument
 * Argument - either named argument or indexed
 * <pre>
 * Flesh out examples... with pretty-printing
 *
 * <h2>Localization</h2>
 * By default, {@link se.softhouse.jargo.Arguments#integerArgument(String...)}, {@link se.softhouse.jargo.Arguments#longArgument(String...)} and so on,
 * formats numbers with {@link se.softhouse.comeon.strings.Describers#numberDescriber()} in a {@link java.util.Locale} sensitive way. If this isn't what you want you can override this with
 * {@link se.softhouse.jargo.ArgumentBuilder#defaultValueDescriber(se.softhouse.comeon.strings.Describer) defaultValueDescriber(Describers.toStringDescriber()}.
 *
 * <h2>API compatibility notes</h2>
 * Public methods that have Guava types in their method signature such as:
 * {@link se.softhouse.jargo.ArgumentBuilder#limitTo(com.google.common.base.Predicate)}
 * {@link se.softhouse.jargo.ArgumentBuilder#defaultValueSupplier(com.google.common.base.Supplier)}
 * {@link se.softhouse.jargo.StringParsers#asFunction(StringParser)}
 * May be changed when JDK 8 is here as it will contain those interfaces without the need of Guava. For now they are marked with {@link com.google.common.annotations.Beta}
 * </pre>
 */
@ParametersAreNonnullByDefault
package se.softhouse.jargo;

import javax.annotation.ParametersAreNonnullByDefault;

