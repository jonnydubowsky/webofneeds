/*
 * Copyright 2012  Research Studios Austria Forschungsges.m.b.H.
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

package won.bot.framework.eventbot.filter.impl;

import won.bot.framework.eventbot.filter.EventFilter;
import won.bot.framework.eventbot.EventListenerContext;

/**
 * Abstract base class for filters that need the event listener context for making their decision.
 */
public abstract class EventListenerContextAwareFilter implements EventFilter
{
  private EventListenerContext context;

  protected EventListenerContextAwareFilter(final EventListenerContext context)
  {
    this.context = context;
  }

  protected EventListenerContext getContext()
  {
    return context;
  }
}
