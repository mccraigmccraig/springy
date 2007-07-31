require 'java'
require 'yaml'

require 'springy/context/serialization'

# This module defines all DSL methods plus supporting classes.
module Springy

    #-- Java imports
    # :stopdoc:
    PropertyValue                = org.springframework.beans.PropertyValue
    RuntimeBeanReference         = org.springframework.beans.factory.config.RuntimeBeanReference
    BeanDefinitionParserDelegate = org.springframework.beans.factory.xml.BeanDefinitionParserDelegate

    # :startdoc:

    # Defaults for newly created beans.
    BEANDEFAULTS = {
        :scope => "singleton",
        :abstract => false,
        :lazy_init => false,
        :dependency_check => "none",
        :autowire => "no",
        :init_method => nil,
        :destroy_method => nil }

    # JRuby initialisers defined for beans.
    INITIALISERS = Hash.new


    # A Springy specific bean definition, subclassing Spring's RootBeanDefinition.
    #
    class BeanDef < Java::springy.context.AbstractSerializableRootBeanDefinition
        attr_reader :bean_id

        AUTOWIRE_MODES = {
            AUTOWIRE_NO =>      "no",
            AUTOWIRE_BY_NAME     => BeanDefinitionParserDelegate::AUTOWIRE_BY_NAME_VALUE,
            AUTOWIRE_BY_TYPE     => BeanDefinitionParserDelegate::AUTOWIRE_BY_TYPE_VALUE,
            AUTOWIRE_CONSTRUCTOR => BeanDefinitionParserDelegate::AUTOWIRE_CONSTRUCTOR_VALUE,
            AUTOWIRE_AUTODETECT  => BeanDefinitionParserDelegate::AUTOWIRE_AUTODETECT_VALUE,
        }

        DEPENDENCY_CHECK_MODES = {
            DEPENDENCY_CHECK_NONE   => "none",
            DEPENDENCY_CHECK_OBJECTS => BeanDefinitionParserDelegate::DEPENDENCY_CHECK_OBJECTS_ATTRIBUTE_VALUE,
            DEPENDENCY_CHECK_SIMPLE  => BeanDefinitionParserDelegate::DEPENDENCY_CHECK_SIMPLE_ATTRIBUTE_VALUE,
            DEPENDENCY_CHECK_ALL     => BeanDefinitionParserDelegate::DEPENDENCY_CHECK_ALL_ATTRIBUTE_VALUE
        }

        def initialize(id, classname, options)
            super()

            @options = options
            setInitMethodName(@options[:init_method]) if @options[:init_method]
            setDestroyMethodName(@options[:destroy_method]) if @options[:destroy_method]
            setLazyInit(@options[:lazy_init])
            setScope(@options[:scope].to_s)
            setAbstract(@options[:abstract])
            setBeanClass(Java::JavaClass.for_name(classname))
            setResourceDescription(caller(3).join(","))
            setAutowireMode(AUTOWIRE_MODES.invert[@options[:autowire]]) if @options[:autowire] != AUTOWIRE_MODES[AUTOWIRE_NO]
            setDependencyCheck(DEPENDENCY_CHECK_MODES.invert[@options[:dependency_check]]) if @options[:dependency_check] != DEPENDENCY_CHECK_MODES[DEPENDENCY_CHECK_NONE]

            setEnforceInitMethod(BEANDEFAULTS[:init_method] != options[:init_method])
            setEnforceDestroyMethod(BEANDEFAULTS[:destroy_method] != options[:destroy_method])
            @bean_id = id ? id.to_s : generate_name
            @auto_generated = id.nil?
        end

        protected
        def generate_name
            org.springframework.beans.factory.support.BeanDefinitionReaderUtils.
                generateBeanName(self, $bean_factory, false)
        end

        def method_missing(symbol, *args)
            symbol = symbol.to_s.sub(/=/, "")

            if args.kind_of?(Hash)
                args[:name] = symbol
            else
                newargs = {}
                newargs[:value] = block_given? ? yield : args.first
                newargs[:name] = symbol
                args = newargs
            end
            property(args)
        end

        public
        # Sets a property on a bean.
        def property(args)
            if args.has_key?(:ref) || args[:value].kind_of?(Symbol)
                ref = SerializableRuntimeBeanReference.new(args[:ref] || args[:value].to_s)
                get_property_values.add_property_value(SerializablePropertyValue.new(args[:name], ref))
            elsif args.has_key?(:value)
                get_property_values.add_property_value(SerializablePropertyValue.new(args[:name], wrap(args[:value])))
            end
            self
        end

        # Constructor-based initialisation.
        def new(*args)
            args.each_with_index do |obj,i|              
                get_constructor_argument_values.add_indexed_argument_value(i, wrap(obj))
            end
            self
        end
        alias_method :constructor, :new

        # Registers an initialiser for the given bean.        
        def initialiser(*args, &block)
            INITIALISERS[@bean_id] = block
        end
    end

    # A post processor which calls all registered JRuby initialisers.
    class InitialiserPostProcessor
        include Java::org.springframework.beans.factory.config.BeanPostProcessor

        def postProcessBeforeInitialization(bean, name)
            block = INITIALISERS[name]
            block.call(bean) if block
            $springy_before_init.call(bean, name) if $springy_before_init
            bean
        end

        def postProcessAfterInitialization(bean, name)
            #puts "postProcessAfterInitialization(#{bean}, #{name})"
            bean
        end
    end

    class StaticBean
        attr_accessor :invocations
        def initialize
            @invocations = {}
        end
        def method_missing(symbol, *args)
            @invocations[symbol.to_s] = args
        end
    end

    ### protected Helper methods
    protected
    # Recursively converts JRuby objects into Spring-specific containers and
    # runtime references.
    def wrap(obj)
        val = obj
        case val
        when Array
            list = SerializableManagedList.new
            val.each do |v|
                list.add(wrap(v))
            end
            val = list
        when Hash
            map = SerializableManagedMap.new
            val.each_pair do |k,v|
                map.put(wrap(k), wrap(v))
            end
            val = map
        when Symbol
            val = SerializableRuntimeBeanReference.new(val.to_s)
        when Proc
            val = wrap(val.call)
        when Numeric
            val = val.to_s
        end
        val
    end

    # Returns a resource from the classpath as string.
    def get_resource(resource)
        is = do_get_resource(resource)
        raise "resource #{resource} not found!" if is.nil?
        JavaUtilities.get_proxy_class("springy.util.IOHelper").inputStreamToString(is)
    end

    def do_get_resource(name)
      is = bean_factory.get_class.get_resource_as_stream(name) ||
           java.lang.Thread.currentThread.context_class_loader.get_resource_as_stream(name)
      is
    end

    def bean_factory
        $bean_factory
    end

    # Registers a BeanDef with the beanfactory.
    def register_bean(bd)
         bean_factory.register_bean_definition(bd.bean_id, bd)
    end

    ### public DSL toplevel methods ###

    public
    # Sets defaults for all beans.
    def defaults(defaults)
        BEANDEFAULTS.merge!(defaults)
    end

    # Creates a new bean.
    # ==== Example (named bean)
    #   bean :b1, "com.foo.Bar" do |b|
    #       b.new(:b2, :b3)
    #   end
    # ==== Example (anonymous bean)
    #   bean "com.foo.Bar" { |b| b.new(:b2, :b3) }
    #
    def bean(id_or_classname, *a, &block)
        bean_id = nil
        classname = nil
        args = BEANDEFAULTS.dup

        case id_or_classname
        when Symbol
            bean_id = id_or_classname
            classname = a.first
        when String
            classname = id_or_classname
        end

        args.merge!(a.last) if a.last.kind_of?(Hash)
        raise "need classname" unless classname
        bd = BeanDef.new(bean_id, classname, args)
        bd.instance_eval(&block) if block
        register_bean(bd)
        bd
    end

    # Registers an alias for an existing bean.
    def bean_alias(name, new_name)
        bean_factory.register_alias(name.to_s, new_name.to_s)
    end

    # Lets the context invoke static methods on a class.
    # ==== Example
    #   static_bean("java.lang.System") do |b|
    #       b.setProperty("key", "value")
    #   end
    def static_bean(classname) # :yields: bean
        if block_given?
            sb = StaticBean.new
            yield sb
            sb.invocations.each_pair do |name, parameters|
                bean "org.springframework.beans.factory.config.MethodInvokingFactoryBean" do |mb|
                    mb.targetClass = classname
                    mb.targetMethod = name
                    mb.arguments = parameters
                end
            end
        end
    end

    # Inline XML fragments.
    #
    # +xml+:: an object representing the XML to be used. If nil, the result of block will be used.
    # +validation+:: whether to use XML validation. Default is no validation.
    # ==== Example
    #   inline_xml do <<XML
    #       <beans><bean id="b1" class="foo.Baz"/></beans>
    #   <<XML
    #   end
    def inline_xml(xml=nil, validation=0)
        xml ||= yield if block_given?
        reader = org.springframework.beans.factory.xml.XmlBeanDefinitionReader.new(bean_factory)
        reader.set_validation_mode(validation)
        res = org.springframework.core.io.ByteArrayResource.new(java.lang.String.new(xml.to_s).get_bytes)
        reader.load_bean_definitions(res)
    end

    # Registers a new post processor on the beanfactory.
    def register_post_processor(processor)
        bean_factory.add_bean_post_processor(processor)
    end

    # Deserializes classpath resource +resource+.
    def yaml(resource)
        YAML.load(get_resource(resource))
    end

    # Read a java system property.
    def sys_property(name, default=nil)
         ($system_properties[name] if $system_properties && $system_properties[name]) || default
    end

    def register_singleton(name, object)
        bean_factory.register_singleton(name, object)
    end

    # Does this resource exists on the classpath?
    def resource_exists?(name)
        do_get_resource(name) != nil
    end

    # block will be called for each bean in the postprocessing stage
    def before_init(&block)
        $springy_before_init=block
    end
end
###############################################################################
#--
#toplevel init stuff

include Springy
register_post_processor(InitialiserPostProcessor.new)





