// $Id: subsystem.cxx 13881 2010-06-10 16:03:36Z jz $        Joacim Zschimmer, Zschimmer GmbH, http://www.zschimmer.com

#include "spooler.h"

namespace sos {
namespace scheduler {

//----------------------------------------------------------------------string_from_subsystem_state
    
string string_from_subsystem_state( Subsystem_state state )
{
    switch( state )
    {
        case subsys_not_initialized:    return "not_initialized";
        case subsys_initialized:        return "initialized";
        case subsys_loaded:             return "loaded";
        case subsys_active:             return "active";
        case subsys_stopped:            return "stopped";

        default: 
            Z_DEBUG_ONLY( assert("subsystem_state"==NULL) );
            return "Subsystem_state:" + as_string( state );
    }
}

//----------------------------------------------------------------------------Subsystem::Subsystem

Subsystem::Subsystem ( Spooler* spooler, IUnknown* iunknown, Type_code t )
: 
    Abstract_scheduler_object( spooler, iunknown, t ),
    _zero_(this+1)
{
    _spooler->_subsystem_register.add(this);
}

//-----------------------------------------------------------Subsystem::throw_subsystem_state_error

void Subsystem::throw_subsystem_state_error( Subsystem_state state, const string& message_text )
{
    z::throw_xc( "SUBSYSTEM-STATE-ERROR", string_from_subsystem_state( state ), string_from_subsystem_state( _subsystem_state ), message_text );
}

//----------------------------------------------------------------Subsystem::assert_subsystem_state

void Subsystem::assert_subsystem_state( Subsystem_state expected_state, const string& message_text )
{
    if( _subsystem_state != expected_state )  throw_subsystem_state_error( expected_state, message_text );
}

//----------------------------------------------------------------Subsystem::switch_subsystem_state

bool Subsystem::switch_subsystem_state( Subsystem_state new_state )
{
    bool result = false;
    
    if( _subsystem_state != new_state )
    {
        Z_LOGI2( "scheduler", obj_name() << ": switch_subsystem_state " << string_from_subsystem_state ( new_state ) << "\n" );

        try
        {
            switch( new_state )
            {
                case subsys_initialized:
                {
                    assert_subsystem_state( subsys_not_initialized, Z_FUNCTION );

                    result = subsystem_initialize();
                    break;
                }

                case subsys_loaded:
                {
                    if( subsystem_state() < subsys_initialized )  switch_subsystem_state( subsys_initialized );
                    assert_subsystem_state( subsys_initialized, Z_FUNCTION );

                    result = subsystem_load();
                    break;
                }

                case subsys_active:
                {
                    if( subsystem_state() < subsys_loaded )  switch_subsystem_state( subsys_loaded );
                    assert_subsystem_state( subsys_loaded, Z_FUNCTION );

                    result = subsystem_activate();
                    break;
                }

                case subsys_stopped:
                {
                    close();
                    break;
                }

                default:
                    throw_subsystem_state_error( new_state, Z_FUNCTION );
            }

            Z_LOG2( "scheduler", obj_name() << ": state=" << string_from_subsystem_state( _subsystem_state ) << "\n" );
        }
        catch( exception& x )
        {
            //_log->error( message_string( "SCHEDULER-332", obj_name(), string_from_subsystem_state( new_state ) ) );
            z::throw_xc( "SCHEDULER-332", obj_name(), string_from_subsystem_state( new_state ), x );
        }
    }

    return result;
}

//------------------------------------------------------------------Subsystem::subsystem_initialize

bool Subsystem::subsystem_initialize()
{
    z::throw_xc( Z_FUNCTION, obj_name(), "not implemented" );
}

//------------------------------------------------------------------------Subsystem::subsystem_load

bool Subsystem::subsystem_load()
{
    z::throw_xc( Z_FUNCTION, obj_name(), "not implemented" );
}

//--------------------------------------------------------------------Subsystem::subsystem_activate

bool Subsystem::subsystem_activate()
{
    z::throw_xc( Z_FUNCTION, obj_name(), "not implemented" );
}

//---------------------------------------------------------------------------Subsystem::dom_element

xml::Element_ptr Subsystem::dom_element( const xml::Document_ptr& dom_document, const Show_what& show_what ) const
{
    xml::Element_ptr result = dom_document.createElement( "subsystem" );
    result.setAttribute( "name", name() );
    result.setAttribute( "state", string_from_subsystem_state( subsystem_state() ) );

    return result;
}


//----------------------------------------------------------------------------Subsystem::~Subsystem

Subsystem::~Subsystem ()
{
    _spooler->_subsystem_register.remove(this);
}


//---------------------------------------------------------------------Subsystem_register::contains

bool Subsystem_register::contains (const string& subsystem_name) const
{
    Z_FOR_EACH_CONST(Set, _set, it)
        if ((*it)->name() == subsystem_name) return true;
    return false;
}

//---------------------------------------------------------------------Subsystem_register::contains

Subsystem* Subsystem_register::get (const string& subsystem_name) const
{
    Z_FOR_EACH_CONST(Set, _set, it)
        if ((*it)->name() == subsystem_name) return *it;

    z::throw_xc( "SCHEDULER-472", subsystem_name );
}

//-------------------------------------------------------------------------------------------------

} //namespace scheduler
} //namespace sos
