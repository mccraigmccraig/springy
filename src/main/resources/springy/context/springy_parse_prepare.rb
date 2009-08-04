require 'java'
require 'yaml'

require 'springy/context/springy'

# this file is loaded each time a springy context definition is to be parsed.
# it resets global context data, and registers a
# BeanPostProcessor with the [ new ] $bean_factory to run the initializers

# EVIL springy context parsing is NOT THREAD SAFE since it uses a global BeanFactory 
# and other global data

# JRuby initialisers defined for beans.
Springy::reset!
register_post_processor(InitialiserPostProcessor.new)

