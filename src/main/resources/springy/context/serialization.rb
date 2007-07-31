module Springy

    SCHEMAS = {
       "http://www.springframework.org/schema/beans" => "http://www.springframework.org/schema/beans/spring-beans-2.0.xsd",
       "http://www.springframework.org/schema/util"  => "http://www.springframework.org/schema/util/spring-util-2.0.xsd"
    }

    include_class 'springy.context.AbstractSerializableManagedList'
    include_class 'springy.context.AbstractSerializableManagedMap'
    include_class 'springy.context.AbstractSerializableRuntimeBeanReference'
    include_class 'springy.context.AbstractSerializableRootBeanDefinition'
    include_class 'springy.context.AbstractSerializablePropertyValue'
    include_class 'com.sun.org.apache.xerces.internal.dom.DocumentImpl'

    module ValueSerializable
        def serialize_xml(doc)
            doc.tag('value') { self.to_s }
        end
    end

    [ String, Fixnum, TrueClass, FalseClass ].each {|c| c.send(:include, ValueSerializable) }

    # add some syntatic sugar to DocumentImpl in order to make it usable
    # (based on the ruby xml builder)
    JavaUtilities.extend_proxy('com.sun.org.apache.xerces.internal.dom.DocumentImpl') {
        def tag(sym, *args, &block)
            text = nil
            attrs = nil
            sym = "#{sym}:#{args.shift}" if args.first.kind_of?(Symbol)
            args.each do |arg|
                case arg
                when Hash
                  attrs ||= {}
                  attrs.merge!(arg)
                else
                  text ||= ''
                  text << arg.to_s
                end
            end

            tag = self.create_element(sym.to_s)

            if attrs
                attrs.each do |name,value|
                    #$stderr.puts value.inspect
                    tag.set_attribute(name, value.to_s)
                end
            end

            tag = last_element.append_child(tag)

            if block
                begin
                    push(tag)
                    val = block.call
                    set_text(val) if val.kind_of?(String)
                ensure
                    pop
                end
            end
        end

        def comment(data)
            last_element.append_child(self.create_comment(data))
        end

        def set_text(val)
            last_element.set_text_content(val.to_s)
        end

        def serialize(out=java.lang.System.err)
            include_class('com.sun.org.apache.xml.internal.serialize.XMLSerializer')
            serializer = XMLSerializer.new(java.io.PrintWriter.new(out), output_format)
            serializer.serialize(self)
        end

        def serialize_to_s
            bos = java.io.ByteArrayOutputStream.new
            serialize(bos)
            bos.to_string
        end

        def output_format
            include_class('com.sun.org.apache.xml.internal.serialize.OutputFormat')
            of = OutputFormat.new
            of.set_indent(4)
            of.set_encoding('UTF-8')
            of.set_line_width(120)
            of
        end

        def validate!
            factory = javax.xml.validation.SchemaFactory.newInstance('http://www.w3.org/2001/XMLSchema');
            schema_is = do_get_resource('/org/springframework/beans/factory/xml/spring-beans-2.0.xsd')
            raise "could not find schema" unless schema_is
            schema_file = javax.xml.transform.stream.StreamSource.new(schema_is)
            schema = factory.newSchema(schema_file)
            validator = schema.newValidator()
            begin
                validator.validate(javax.xml.transform.dom.DOMSource.new(self))
            rescue NativeException => e
                #$stderr.puts("#{e.cause.line_number}: #{e.cause.message}")
                raise e
            end
        end

        def push(element)
            ensure_stack
            $tag_stack.push(element)
        end

        def pop
            ensure_stack
            $tag_stack.pop
        end

        def ensure_stack
            unless defined?($tag_stack)
                $tag_stack = []
                $tag_stack.push(self)
            end
        end

        def last_element
            ensure_stack
            $tag_stack.last
        end
    }

    class SerializableManagedList < AbstractSerializableManagedList
        def serialize_xml(doc)
            doc.tag('list') do
                self.each do |item|
                    if item.respond_to?(:serialize_xml)
                      item.serialize_xml(doc)
                    end
                end
            end
        end
    end

    class SerializableManagedMap < AbstractSerializableManagedMap
        def serialize_xml(doc)
           doc.tag('map') do
             self.each do |k,v|
                doc.tag('entry', "key"=>k.to_s) do
                    if v.respond_to?(:serialize_xml)
                        v.serialize_xml(doc)
                    end
                end
             end
           end
        end
    end

    class SerializableRuntimeBeanReference < AbstractSerializableRuntimeBeanReference
        def initialize(name)
            super(name)
        end

        def serialize_xml(doc)
            doc.tag('ref', "bean"=>bean_name)
        end
    end

    class SerializablePropertyValue < AbstractSerializablePropertyValue
        def initialize(name, value)
            super
        end

        def serialize_xml(doc)
            if value.respond_to?(:serialize_xml)
                value.serialize_xml(doc)
            end
        end
    end

    class BeanDef < AbstractSerializableRootBeanDefinition

       def get_non_default_attrs
        attrs = {}
        attrs['init-method'] = init_method_name if init_method_name != BEANDEFAULTS[:init_method]
        attrs['destroy-method'] = destroy_method_name if destroy_method_name != BEANDEFAULTS[:destroy_method]
        attrs['scope'] = scope if scope != BEANDEFAULTS[:scope]
        attrs['lazy-init'] = lazy_init if lazy_init != BEANDEFAULTS[:lazy_init]
        attrs['abstract']  = abstract.to_s if abstract != BEANDEFAULTS[:abstract]
        attrs['autowire'] = AUTOWIRE_MODES[autowire_mode] if autowire_mode != 0
        attrs['dependency-check'] = DEPENDENCY_CHECK_MODES[dependency_check] if dependency_check != 0
        attrs
       end

       def serialize_xml(doc)
            attrs = {}
            attrs['id'] = self.bean_id unless @auto_generated
            attrs['class'] = self.bean_class_name

            attrs.merge!(get_non_default_attrs)

            doc.tag("bean", attrs) do
                (0..constructor_argument_values.argument_count-1).each do |i|
                    doc.tag("constructor-arg",  "index" => i.to_s) do
                        v = constructor_argument_values.get_argument_value(i, nil)
                        if v.value.respond_to?(:serialize_xml)
                            v.value.serialize_xml(doc)
                        else
                            v.value.to_s.serialize_xml(doc)
                        end
                    end
                end

                property_values.getPropertyValues.each do |val|
                    doc.tag('property', "name" => val.name) do
                        val.serialize_xml(doc)
                    end
                end
           end
        end
    end

    #Serialize the context to xml.
    def serialize_context(out=java.lang.System.err, validate=true)
        doc = DocumentImpl.new
        begin
            attrs = {
                "xmlns"=>"http://www.springframework.org/schema/beans",
                "xmlns:xsi"=>"http://www.w3.org/2001/XMLSchema-instance"
            }

            attrs['default-init-method']    = BEANDEFAULTS[:init_method]    if BEANDEFAULTS[:init_method]
            attrs['default-destroy-method'] = BEANDEFAULTS[:destroy_method] if BEANDEFAULTS[:destroy_method]

            xsi_schemaLocation=''
            SCHEMAS.each do |k,v|
                xsi_schemaLocation += "#{k} #{v} "
            end

            attrs['xsi:schemaLocation'] = xsi_schemaLocation

            alias_map = {}
            names     = []
            bean_factory.bean_definition_names.each do |name|
              names << name
              alias_map[name] = bean_factory.get_aliases(name)
            end

            #alias_map.each {|name, aliases| names.reject! {|n| aliases.include?(n)}}
            doc.comment("serialized by springy on #{Time.new}")
            doc.tag('beans', attrs) do
                names.each do |name|
                    bd = bean_factory.get_bean_definition(name)
                    bd.serialize_xml(doc) if bd.respond_to?(:serialize_xml)
                    alias_map[name].each { |a| doc.tag('alias', "name"=>name, "alias"=>a) } if alias_map[name]
                end
            end
            doc.validate! if validate
        ensure
            #doc.serialize(out)
        end
        return doc.serialize_to_s, doc
    end
end